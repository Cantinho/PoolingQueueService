package com.cantinho.cms.sqs.impl;

import com.cantinho.cms.bean.Message;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.cantinho.cms.bean.Message.parseMinimalistStringToMessage;
import static com.cantinho.cms.bean.Message.parseToMinimalistString;

/**
 * Copyright 2016 Cantinho. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * @author Samir Trajano Feitosa
 * @author Jordão Ezequiel Serafim de Araújo
 * @author Cantinho - Github https://github.com/Cantinho
 * @since 2016
 * @license Apache 2.0
 *
 * This file is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 */
public class AmazonMessageQueue implements IMessageQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);
    final String masterLock = "MASTER_QUEUE_LOCK";
    final String slaveLock = "SLAVE_QUEUE_LOCK";

    private String queueName = "";
    private AmazonSQSApi amazonSQSApi;

    AmazonMessageQueue(){}

    AmazonMessageQueue(AmazonSQSApi amazonSQSApi) {
        this.amazonSQSApi = amazonSQSApi;
    }

    AmazonMessageQueue(AmazonSQSApi amazonSQSApi, String queueName) {
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

    public void initializeMessageQueue() throws Exception {
        checkAmazonSQSApiInstance();
        amazonSQSApi.createQueue(queueName);
    }

    public boolean produceMessageToMaster(final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        if(amazonSQSApi.listQueues().getQueueUrls().contains(amazonSQSApi.getQueueUrl(queueName))) {
            SendMessageResult sendMessageResult = amazonSQSApi.sendMessage(queueName, parseToMinimalistString(message));
            return sendMessageResult == null;
        }
        return false;
    }

    @Override
    public Message peekMessageOfMaster() throws Exception {
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
    public Message consumeMessageOfMaster() throws Exception {
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

    public List<Message> consumeMessageOfMaster(final int amount) throws Exception {
        checkAmazonSQSApiInstance();
        List<Message> messages = new LinkedList<>();

        synchronized (masterLock) {
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


    public boolean produceMessageToSlave(final String slaveId, final Message message) throws Exception {
        checkAmazonSQSApiInstance();
        final String slaveIdQueueName = queueName + "_" + slaveId;
        boolean containsslaveId = amazonSQSApi.listQueues().getQueueUrls()
                .contains(amazonSQSApi.getQueueUrl(slaveIdQueueName));
        synchronized (slaveLock) {
            return sendMessage(slaveId, containsslaveId, message);
        }
    }

    @Override
    public boolean broadcastMessageToSlave(final String slaveIdOrigin, final Message message) throws Exception {
        checkAmazonSQSApiInstance();

        synchronized (slaveLock) {
            final String slaveIdQueueName = queueName + "_" + slaveIdOrigin;
            boolean broadcasted = false;
            boolean slaveIdOriginFound = amazonSQSApi.listQueues(slaveIdQueueName).getQueueUrls()
                    .contains(amazonSQSApi.getQueueUrl(slaveIdQueueName));
            List<String> allslaveQueueUrls = amazonSQSApi.listQueues().getQueueUrls();

            // TODO analisar url

            List<String> slaveQueueNamesFound = new ArrayList<>();
            Iterator<String> slaveNamesIterator = allslaveQueueUrls.iterator();
            while(slaveNamesIterator.hasNext()) {
                final String currentSlaveUrl = slaveNamesIterator.next();
                if(currentSlaveUrl.contains(queueName) && !currentSlaveUrl.contains(slaveIdQueueName)) {
                    // adds only the slaveId different than slaveIdOrigin.
                    // slaveIdOrigin will be used if slaveIdOriginFound is true;
                    // If slaveIdOrigin is false, slaveQueue will be created.

                    final String currentSlaveName = currentSlaveUrl.replace(amazonSQSApi.getBaseUrl() +
                            "/" + amazonSQSApi.getAccountId() + "/", "").replace("." + amazonSQSApi.getQueueType(), "");

                    slaveQueueNamesFound.add(currentSlaveName);
                }
            }
            Iterator<String> slaveNamesFoundIterator = slaveQueueNamesFound.iterator();
            while(slaveNamesFoundIterator.hasNext()) {
                broadcasted = true;
                amazonSQSApi.sendMessage(slaveIdQueueName, parseToMinimalistString(message));
            }
            if(slaveIdOrigin != null) {
                broadcasted = sendMessage(slaveIdOrigin, slaveIdOriginFound, message);
            }
            return broadcasted;
        }
    }

    private boolean sendMessage(final String slaveIdOrigin, final boolean slaveIdAlreadyExists, final Message message) {
        final String slaveIdQueueName = queueName + "_" + slaveIdOrigin;
        if (slaveIdAlreadyExists) {
            return null != amazonSQSApi.sendMessage(slaveIdQueueName, parseToMinimalistString(message));
        } else {
            boolean wasQueueCreated = null != amazonSQSApi.createQueue(slaveIdQueueName);
            if(!wasQueueCreated) {
                LOGGER.info("Queue [" + slaveIdQueueName + "] was not created.");
                return false;
            }
            return null != amazonSQSApi.sendMessage(slaveIdQueueName, parseToMinimalistString(message));
        }
    }

    public Message peekMessageOfSlave(final String slaveId) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE

        /* synchronized (slaveLock) {
            final String slaveIdQueueName = queueName + "_" + slaveId;
            boolean containsslaveId = amazonSQSApi.listQueues(slaveIdQueueName).getQueueUrls()
                    .contains(slaveIdQueueName);
            if(containsslaveId) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(slaveIdQueueName, 1);
                return retrievedMessages == null || retrievedMessages.isEmpty() ? null : retrievedMessages.get(0);
            } else {
                return null;
            }
        }*/

        return null; //TODO REMOVE THIS AFTER FIX ENTIRE METHOD
    }

    public Message consumeMessageOfSlave(final String slaveId) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE

        /*synchronized (slaveLock) {
            final String slaveIdQueueName = queueName + "_" + slaveId;
            boolean containsslaveId = amazonSQSApi.listQueues().contains(slaveIdQueueName);
            if (containsslaveId) {
                List<Message> retrievedMessages = amazonSQSApi.receiveMessage(slaveIdQueueName, 1);
                if(retrievedMessages == null || retrievedMessages.isEmpty()){
                    return null;
                } else {
                    amazonSQSApi.deleteMessage(slaveIdQueueName, retrievedMessages.get(0));
                    return retrievedMessages.get(0);
                }
            }
        }*/
        return null;
    }

    public List<Message> consumeMessageOfSlave(final String slaveId, final int amount) throws Exception {
        checkAmazonSQSApiInstance();

        //TODO FIXME PLEASE
        /*List<Message> retrievedMessages = new LinkedList<>();
        synchronized (slaveLock) {
            final String slaveIdQueueName = queueName + "_" + slaveId;
            boolean containsslaveId = amazonSQSApi.listQueues().contains(slaveIdQueueName);
            if(containsslaveId) {
                List<Message> receivedMessages = amazonSQSApi.receiveMessage(slaveIdQueueName, amount);
                if(!receivedMessages.isEmpty()) {
                    amazonSQSApi.deleteMessage(slaveIdQueueName, receivedMessages);
                }
                return receivedMessages;
            }

            return retrievedMessages;
        }*/

        return null; //TODO REMOVE THIS AFTER FIX ENTIRE METHOD
    }

    @Override
    public String addSlaveMessageQueue(String slaveId) throws Exception {
        //TODO FIXME PLEASE
        return null;
    }

}
