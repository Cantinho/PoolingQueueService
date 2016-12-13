package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static br.com.pqs.bean.Message.parseMinimalistStringToMessage;
import static br.com.pqs.bean.Message.parseToMinimalistString;

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
        if(amazonSQSApi.listQueues().getQueueUrls().contains(amazonSQSApi.getQueueUrl(queueName))) {
            SendMessageResult sendMessageResult = amazonSQSApi.sendMessage(queueName, parseToMinimalistString(message));
            return sendMessageResult == null;
        }
        return false;
    }

    @Override
    public Message peekMessageOfCentral() throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().getQueueUrls().contains(amazonSQSApi.getQueueUrl(queueName))) {
            List<com.amazonaws.services.sqs.model.Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, 1);
            List<Message> parsedMessages = new ArrayList<>();
            Iterator<com.amazonaws.services.sqs.model.Message> messageIterator = retrievedMessages.iterator();
            while(messageIterator.hasNext()) {
                parsedMessages.add(parsedMessages.size(), parseMinimalistStringToMessage(messageIterator.next().getBody()));
            }

            if(parsedMessages.isEmpty()) {
                return null;
            }
            return parsedMessages.get(0);
        }
        return null;
    }

    @Override
    public Message consumeMessageOfCentral() throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().getQueueUrls().contains(amazonSQSApi.getQueueUrl(queueName))) {
            List<com.amazonaws.services.sqs.model.Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, 1);
            if(retrievedMessages.isEmpty()) {
                return null;
            }
            com.amazonaws.services.sqs.model.Message firstMessage = retrievedMessages.get(0);
            if(firstMessage == null) {
                return null;
            }
            amazonSQSApi.deleteMessage(queueName, firstMessage.getReceiptHandle());
            return parseMinimalistStringToMessage(firstMessage.getBody());
        }
        return null;
    }

    public List<Message> consumeMessageOfCentral(final int amount) throws Exception {
        checkAmazonSQSApiInstance();
        List<Message> messages = new LinkedList<>();

        synchronized (centralLock) {
            if(amazonSQSApi.listQueues().getQueueUrls().contains(amazonSQSApi.getQueueUrl(queueName))) {
                List<Message> parsedMessages = new ArrayList<>();
                List<com.amazonaws.services.sqs.model.Message> retrievedMessages = amazonSQSApi.receiveMessage(queueName, amount);
                if(retrievedMessages.isEmpty()) {
                    return null;
                }
                List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries = new ArrayList<>();
                Iterator<com.amazonaws.services.sqs.model.Message> messageIterator = retrievedMessages.iterator();
                while(messageIterator.hasNext()) {
                    com.amazonaws.services.sqs.model.Message currentMessage = messageIterator.next();
                    parsedMessages.add(parsedMessages.size(), parseMinimalistStringToMessage(currentMessage.getBody()));
                    deleteMessageBatchRequestEntries.add(deleteMessageBatchRequestEntries.size(),
                            new DeleteMessageBatchRequestEntry(currentMessage.getMessageId(),
                                    currentMessage.getReceiptHandle()));
                }

                parsedMessages.add(parseMinimalistStringToMessage(retrievedMessages.get(0).getBody()));
                amazonSQSApi.deleteMessageBatch(queueName, deleteMessageBatchRequestEntries);
                return parsedMessages;
            }
            return messages;
        }
    }


    public boolean produceMessageToApplication(final String applicationId, final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        final String applicationIdQueueName = queueName + "_" + applicationId;
        boolean containsApplicationId = amazonSQSApi.listQueues().getQueueUrls()
                .contains(amazonSQSApi.getQueueUrl(applicationIdQueueName));
        synchronized (applicationLock) {
            return sendMessage(applicationId, containsApplicationId, message);
        }
    }

    @Override
    public boolean broadcastMessageToApplication(final String applicationIdOrigin, final Message message) throws Exception {
        checkAmazonSQSApiInstance();

        synchronized (applicationLock) {
            final String applicationIdQueueName = queueName + "_" + applicationIdOrigin;
            boolean broadcasted = false;
            boolean applicationIdOriginFound = amazonSQSApi.listQueues(applicationIdQueueName).getQueueUrls()
                    .contains(amazonSQSApi.getQueueUrl(applicationIdQueueName));
            List<String> allApplicationQueueUrls = amazonSQSApi.listQueues().getQueueUrls();

            // TODO analisar url

            List<String> applicationQueueNamesFound = new ArrayList<>();
            Iterator<String> applicationNamesIterator = allApplicationQueueUrls.iterator();
            while(applicationNamesIterator.hasNext()) {
                final String currentApplicationUrl = applicationNamesIterator.next();
                if(currentApplicationUrl.contains(queueName) && !currentApplicationUrl.contains(applicationIdQueueName)) {
                    // adds only the applicationId different than applicationIdOrigin.
                    // ApplicationIdOrigin will be used if applicationIdOriginFound is true;
                    // If ApplicationIdOrigin is false, applicationQueue will be created.

                    final String currentApplicationName = currentApplicationUrl.replace(amazonSQSApi.getBaseUrl() +
                            "/" + amazonSQSApi.getAccountId() + "/", "").replace("." + amazonSQSApi.getQueueType(), "");

                    applicationQueueNamesFound.add(currentApplicationName);
                }
            }
            Iterator<String> applicationNamesFoundIterator = applicationQueueNamesFound.iterator();
            while(applicationNamesFoundIterator.hasNext()) {
                broadcasted = true;
                amazonSQSApi.sendMessage(applicationIdQueueName, parseToMinimalistString(message));
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
            return null != amazonSQSApi.sendMessage(applicationIdQueueName, parseToMinimalistString(message));
        } else {
            boolean wasQueueCreated = null != amazonSQSApi.createQueue(applicationIdQueueName);
            if(!wasQueueCreated) {
                LOGGER.info("Queue [" + applicationIdQueueName + "] was not created.");
                return false;
            }
            return null != amazonSQSApi.sendMessage(applicationIdQueueName, parseToMinimalistString(message));
        }
    }

    public Message peekMessageOfApplication(final String applicationId) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE

        /* synchronized (applicationLock) {
            final String applicationIdQueueName = queueName + "_" + applicationId;
            boolean containsApplicationId = amazonSQSApi.listQueues(applicationIdQueueName).getQueueUrls()
                    .contains(applicationIdQueueName);
            if(containsApplicationId) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(applicationIdQueueName, 1);
                return retrievedMessages == null || retrievedMessages.isEmpty() ? null : retrievedMessages.get(0);
            } else {
                return null;
            }
        }*/

        return null; //TODO REMOVE THIS AFTER FIX ENTIRE METHOD
    }

    public Message consumeMessageOfApplication(final String applicationId) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE

        /*synchronized (applicationLock) {
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
        }*/
        return null;
    }

    public List<Message> consumeMessageOfApplication(final String applicationId, final int amount) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE
        /*List<Message> retrievedMessages = new LinkedList<>();
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
        }*/

        return null; //TODO REMOVE THIS AFTER FIX ENTIRE METHOD
    }

    @Override
    public String addApplicationPoolingQueue(String applicationID) throws Exception {
        //TODO FIXME PLEASE
        return null;
    }

}
