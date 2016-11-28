package br.com.example.sqs.impl;

import br.com.example.bean.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by samirtf on 27/11/16.
 */
public class AmazonPoolingQueue implements IPoolingQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(PoolingQueue.class);
    final String centralLock = "CENTRAL_QUEUE_LOCK";
    final String applicationLock = "APPLICATION_QUEUE_LOCK";

    private String queueName = "";
    private AmazonSQSApi amazonSQSApi;

    AmazonPoolingQueue(){}

    AmazonPoolingQueue(AmazonSQSApi amazonSQSApi) {
        this.amazonSQSApi = amazonSQSApi;
    }

    AmazonPoolingQueue(AmazonSQSApi amazonSQSApi, String queueName) {
        this.amazonSQSApi = amazonSQSApi;
        this.queueName = queueName;
    }

    void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    void setAmazonSQSApi(AmazonSQSApi amazonSQSApi) {
        this.amazonSQSApi = amazonSQSApi;
    }

    private void checkAmazonSQSApiInstance() throws Exception {
        if(amazonSQSApi == null) {
            throw new Exception("AmazonSQSApiInstance is null or was not initialized.");
        }
    }

    public void initializePoolingQueue() throws Exception {
        checkAmazonSQSApiInstance();
        amazonSQSApi.createQueue(queueName);
    }

    public boolean produceMessageToCentral(final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().contains(queueName)) {
            return amazonSQSApi.sendMessage(queueName, message);
        }
        return false;
    }

    @Override
    public Message peekMessageOfCentral() throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().contains(queueName)) {
            List<Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, 1);
            if(retrievedMessages.isEmpty()) {
                return null;
            }
            return retrievedMessages.get(0);
        }
        return null;
    }

    @Override
    public Message consumeMessageOfCentral() throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().contains(queueName)) {
            List<Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, 1);
            if(retrievedMessages.isEmpty()) {
                return null;
            }
            amazonSQSApi.deleteMessage(queueName, retrievedMessages.get(0));
            return retrievedMessages.get(0);
        }
        return null;
    }

    public List<Message> consumeMessageOfCentral(final int amount) throws Exception {
        checkAmazonSQSApiInstance();
        List<Message> messages = new LinkedList<>();

        synchronized (centralLock) {
            if(amazonSQSApi.listQueues().contains(queueName)) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, amount);
                if(retrievedMessages.isEmpty()) {
                    return null;
                }
                amazonSQSApi.deleteMessage(queueName, retrievedMessages);
                return retrievedMessages;
            }
            return messages;
        }
    }


    public boolean produceMessageToApplication(final String applicationId, final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        final String applicationIdQueueName = queueName + "_" + applicationId;
        boolean containsApplicationId = amazonSQSApi.listQueues().contains(applicationIdQueueName);
        synchronized (applicationLock) {
            return sendMessage(applicationId, containsApplicationId, message);
        }
    }

    @Override
    public boolean broadcastMessageToApplication(final String applicationIdOrigin, final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        final String applicationIdQueueName = queueName + "_" + applicationIdOrigin;
        synchronized (applicationLock) {
            boolean broadcasted = false;
            boolean applicationIdOriginFound = amazonSQSApi.listQueues().contains(applicationIdQueueName);;
            List<String> allApplicationQueueNames = amazonSQSApi.listQueues();
            List<String> applicationQueueNamesFound = new ArrayList<>();
            Iterator<String> applicationNamesIterator = allApplicationQueueNames.iterator();
            while(applicationNamesIterator.hasNext()) {
                String currentApplicationName = applicationNamesIterator.next();
                if(currentApplicationName.startsWith(queueName) && !currentApplicationName.equals(applicationIdQueueName)) {
                    // adds only the applicationId different than applicationIdOrigin.
                    // ApplicationIdOrigin will be used if applicationIdOriginFound is true;
                    // If ApplicationIdOrigin is false, applicationQueue will be created.
                    applicationQueueNamesFound.add(currentApplicationName);
                }
            }
            Iterator<String> applicationNamesFoundIterator = applicationQueueNamesFound.iterator();
            while(applicationNamesFoundIterator.hasNext()) {
                broadcasted = true;
                amazonSQSApi.sendMessage(applicationIdQueueName, message);
            }
            if(applicationIdOrigin != null) {
                broadcasted = sendMessage(applicationIdOrigin, applicationIdOriginFound, message);
            }
            return broadcasted;
        }
    }

    private boolean sendMessage(final String applicationIdOrigin, final boolean applicationIdAlreadyExists, final Message message) {
        final String applicationIdQueueName = queueName + "_" + applicationIdOrigin;
        if (applicationIdAlreadyExists) {
            return amazonSQSApi.sendMessage(applicationIdQueueName, message);
        } else {
            boolean wasQueueCreated = amazonSQSApi.createQueue(applicationIdQueueName);
            if(!wasQueueCreated) {
                LOGGER.info("Queue [" + applicationIdQueueName + "] was not created.");
                return false;
            }
            return amazonSQSApi.sendMessage(applicationIdQueueName, message);
        }
    }

    public Message peekMessageOfApplication(final String applicationId) throws Exception {
        checkAmazonSQSApiInstance();
        synchronized (applicationLock) {
            final String applicationIdQueueName = queueName + "_" + applicationId;
            boolean containsApplicationId = amazonSQSApi.listQueues().contains(applicationIdQueueName);
            if(containsApplicationId) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(applicationIdQueueName, 1);
                return retrievedMessages == null || retrievedMessages.isEmpty() ? null : retrievedMessages.get(0);
            } else {
                return null;
            }
        }
    }

    public Message consumeMessageOfApplication(final String applicationId) throws Exception {
        checkAmazonSQSApiInstance();
        synchronized (applicationLock) {
            final String applicationIdQueueName = queueName + "_" + applicationId;
            boolean containsApplicationId = amazonSQSApi.listQueues().contains(applicationIdQueueName);
            if (containsApplicationId) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(applicationIdQueueName, 1);
                if(retrievedMessages == null || retrievedMessages.isEmpty()){
                    return null;
                } else {
                    amazonSQSApi.deleteMessage(applicationIdQueueName, retrievedMessages.get(0));
                    return retrievedMessages.get(0);
                }
            }
        }
        return null;
    }

    public List<Message> consumeMessageOfApplication(final String applicationId, final int amount) throws Exception {
        checkAmazonSQSApiInstance();
        List<Message> retrievedMessages = new LinkedList<>();
        synchronized (applicationLock) {
            final String applicationIdQueueName = queueName + "_" + applicationId;
            boolean containsApplicationId = amazonSQSApi.listQueues().contains(applicationIdQueueName);
            if(containsApplicationId) {
                List<Message> receivedMessages = amazonSQSApi.receiveMessage(applicationIdQueueName, amount);
                if(!receivedMessages.isEmpty()) {
                    amazonSQSApi.deleteMessage(applicationIdQueueName, receivedMessages);
                }
                return receivedMessages;
            }

            return retrievedMessages;
        }
    }

}
