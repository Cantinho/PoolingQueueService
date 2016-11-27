package br.com.example.sqs.impl;

import br.com.example.bean.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
        return centralQueue.add(message);
    }

    public Message peekMessageOfCentral() throws Exception {
        validateQueueName();
        return centralQueue.peek();
    }

    public Message consumeMessageOfCentral() throws Exception {
        validateQueueName();
        return centralQueue.poll();
    }

    public List<Message> consumeMessageOfCentral(final int amount) throws Exception {
        validateQueueName();
        List<Message> messages = new LinkedList<>();
        synchronized (centralLock) {
            int counter = amount;
            Iterator<Message> messageIterator = centralQueue.iterator();
            while(messageIterator.hasNext()) {
                messages.add(messages.size(), messageIterator.next());
                counter--;
                if(counter == 0) {
                    break;
                }
            }
        }
        return messages;
    }

    public boolean produceMessageToApplication(final String applicationId, final Message message) throws Exception {
        validateQueueName();
        boolean containsApplicationId = applicationQueues.containsKey(applicationId);
        synchronized (applicationLock) {
            if (containsApplicationId) {
                return applicationQueues.get(applicationId).add(message);
            } else {
                Queue<Message> newApplicationQueue = new LinkedBlockingQueue<>();
                boolean wasMessageAdded = newApplicationQueue.add(message);
                if(applicationQueues.put(applicationId, newApplicationQueue) == null) {
                    LOGGER.info("There wasn't a previously queue with applicationId " + applicationId);
                };
                return wasMessageAdded;
            }
        }
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
                return applicationQueues.get(applicationId).poll();
            }
        }
        return null;
    }

    public List<Message> consumeMessageOfApplication(final String applicationId, final int amount) throws Exception {
        validateQueueName();
        List<Message> messagesRetrieved = new LinkedList<>();
        synchronized (applicationLock) {
            Queue<Message> retrievedApplicationQueue = applicationQueues.get(applicationId);
            if(retrievedApplicationQueue != null) {
                Iterator<Message> messageIterator = retrievedApplicationQueue.iterator();
                int counter = amount;
                while(messageIterator.hasNext()) {
                    // Adding message at last position to keep the correct order of messages.
                    messagesRetrieved.add(messagesRetrieved.size(), messageIterator.next());
                    counter--;
                    if(counter <= 0) {
                        return messagesRetrieved;
                    }
                }
            }
            return messagesRetrieved;
        }
    }

}
