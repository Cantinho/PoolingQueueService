package br.com.example.sqs.impl;

import br.com.example.bean.Message;
import br.com.example.exceptions.PoolingQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static br.com.example.exceptions.PoolingQueueException.APPLICATION_NOT_FOUND;

/**
 * Created by samirtf on 26/11/16.
 */
public class PoolingQueue implements IPoolingQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(PoolingQueue.class);

    final String centralLock = "CENTRAL_QUEUE_LOCK";
    final String applicationLock = "APPLICATION_QUEUE_LOCK";

    private String queueName;
    private Queue<Message> centralQueue = new LinkedBlockingQueue<>();
    private Map<String, Queue<Message>> applicationQueues = new TreeMap<>();

    PoolingQueue() {}

    PoolingQueue(String queueName) {
        this.queueName = queueName;
    }

    private void validateQueueName() throws Exception {
        if(queueName == null || queueName.trim().isEmpty()) {
            throw new Exception("Must have a valid name for queue.");
        }
    }

    @Override
    public boolean produceMessageToCentral(final Message message) throws Exception {
        validateQueueName();
        boolean produced = centralQueue.add(message);
        LOGGER.info("Central [" + queueName + "] has " + centralQueue.size() + " messages");
        return produced;
    }

    public Message peekMessageOfCentral() throws Exception {
        validateQueueName();
        return centralQueue.peek();
    }

    public Message consumeMessageOfCentral() throws Exception {
        validateQueueName();
        Message retrievedMessage = centralQueue.poll();
        LOGGER.info("Central [" + queueName + "] has " + centralQueue.size() + " messages");
        return retrievedMessage;
    }

    public List<Message> consumeMessageOfCentral(final int amount) throws Exception {
        validateQueueName();
        List<Message> retrievedMessages = new LinkedList<>();
        synchronized (centralLock) {
            int counter = amount;
            while(counter > 0) {
                Message retrievedMessage = centralQueue.poll();
                if(retrievedMessage == null) {
                    LOGGER.info("Central [" + queueName + "] has " + centralQueue.size() + " messages");
                    return retrievedMessages;
                }
                retrievedMessages.add(retrievedMessages.size(), retrievedMessage);
                counter--;
                if(counter <= 0) {
                    LOGGER.info("Central [" + queueName + "] has " + centralQueue.size() + " messages");
                    return retrievedMessages;
                }
            }
            LOGGER.info("Central [" + queueName + "] has " + centralQueue.size() + " messages");
        }
        return retrievedMessages;
    }

    public boolean produceMessageToApplication(final String applicationId, final Message message) throws Exception {
        validateQueueName();
        boolean containsApplicationId = applicationQueues.containsKey(applicationId);
        synchronized (applicationLock) {
            if (containsApplicationId) {
                boolean produced = applicationQueues.get(applicationId).add(message);
                LOGGER.info("Application queue [" + applicationId + "] has " + applicationQueues.get(applicationId).size() + " messages");
                return produced;
            } else {
                return createApplicationQueueAndTrySendMessage(applicationId, message);
            }
        }
    }

    private boolean createApplicationQueueAndTrySendMessage(final String applicationId, final Message message) {
        Queue<Message> newApplicationQueue = new LinkedBlockingQueue<>();
        boolean wasMessageAdded = newApplicationQueue.add(message);
        LOGGER.info("Application queue [" + applicationId + "] has " + newApplicationQueue.size() + " messages");
        if (applicationQueues.put(applicationId, newApplicationQueue) == null) {
            LOGGER.info("There wasn't a previously queue with applicationId " + applicationId);
        }
        return wasMessageAdded;
    }

    @Override
    public boolean broadcastMessageToApplication(final String applicationIdOrigin, final Message message) throws Exception {
        validateQueueName();
        synchronized (applicationLock) {
            boolean containsApplicationId = false;
            Iterator<String> applicationQueuesIterator = applicationQueues.keySet().iterator();
            while(applicationQueuesIterator.hasNext()) {
                final String applicationQueueName = applicationQueuesIterator.next();
                if(applicationIdOrigin.equals(applicationQueueName)) {
                    containsApplicationId = true;
                }
                applicationQueues.get(applicationQueueName).add(message);
            }
            if(!containsApplicationId) {
                createApplicationQueueAndTrySendMessage(applicationIdOrigin, message);
            }
        }
        return true;
    }

    public Message peekMessageOfApplication(final String applicationId) throws Exception {
        validateQueueName();
        Queue<Message> messageQueue = applicationQueues.get(applicationId);
        return messageQueue == null ? null : messageQueue.peek();
    }

    public Message consumeMessageOfApplication(final String applicationId) throws Exception {
        validateQueueName();
        synchronized (applicationLock) {
            boolean containsApplicationId = applicationQueues.containsKey(applicationId);
            if (containsApplicationId) {
                Message messagePolled = applicationQueues.get(applicationId).poll();
                LOGGER.info("Application queue [" + applicationId + "] has " + applicationQueues.get(applicationId).size() + " messages");
                return messagePolled;
            }
        }
        throw new PoolingQueueException("", APPLICATION_NOT_FOUND);
    }

    public List<Message> consumeMessageOfApplication(final String applicationId, final int amount) throws Exception {
        validateQueueName();
        List<Message> retrievedMessages = new LinkedList<>();
        synchronized (applicationLock) {
            Queue<Message> retrievedApplicationQueue = applicationQueues.get(applicationId);
            if(retrievedApplicationQueue != null) {
                int counter = amount;
                while(counter > 0) {
                    Message retrievedMessage = retrievedApplicationQueue.poll();
                    if(retrievedMessage == null) {
                        LOGGER.info("Application queue [" + applicationId + "] has " + retrievedApplicationQueue.size() + " messages");
                        return retrievedMessages;
                    }
                    retrievedMessages.add(retrievedMessages.size(), retrievedMessage);
                    counter--;
                    if(counter <= 0) {
                        LOGGER.info("Application queue [" + applicationId + "] has " + retrievedApplicationQueue.size() + " messages");
                        return retrievedMessages;
                    }
                }
            }
            LOGGER.info("Application queue [" + applicationId + "] has 0 messages");
            throw new PoolingQueueException("", APPLICATION_NOT_FOUND);
        }
    }

}
