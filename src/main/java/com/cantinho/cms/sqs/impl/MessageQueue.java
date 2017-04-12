package com.cantinho.cms.sqs.impl;

import com.cantinho.cms.bean.Message;
import com.cantinho.cms.exceptions.CloudiaMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
public class MessageQueue implements IMessageQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);

    final String masterLock = "MASTER_QUEUE_LOCK";
    final String slaveLock = "SLAVE_QUEUE_LOCK";

    private String queueName;
    private Queue<Message> masterQueue = new LinkedBlockingQueue<>();
    private Map<String, Queue<Message>> slaveQueues = new TreeMap<>();

    MessageQueue() {}

    MessageQueue(String queueName) {
        this.queueName = queueName;
    }

    private void validateQueueName() throws Exception {
        if(queueName == null || queueName.trim().isEmpty()) {
            throw new Exception("Must have a valid name for queue.");
        }
    }

    @Override
    public boolean produceMessageToMaster(final Message message) throws Exception {
        validateQueueName();
        boolean produced = masterQueue.add(message);
        LOGGER.info("Master [" + queueName + "] has " + masterQueue.size() + " messages");
        return produced;
    }

    public Message peekMessageOfMaster() throws Exception {
        validateQueueName();
        return masterQueue.peek();
    }

    public Message consumeMessageOfMaster() throws Exception {
        validateQueueName();
        Message retrievedMessage = masterQueue.poll();
        LOGGER.info("Master [" + queueName + "] has " + masterQueue.size() + " messages");
        return retrievedMessage;
    }

    public List<Message> consumeMessageOfMaster(final int amount) throws Exception {
        validateQueueName();
        List<Message> retrievedMessages = new LinkedList<>();
        synchronized (masterLock) {
            int counter = amount;
            while(counter > 0) {
                Message retrievedMessage = masterQueue.poll();
                if(retrievedMessage == null) {
                    LOGGER.info("Master [" + queueName + "] has " + masterQueue.size() + " messages");
                    return retrievedMessages;
                }
                retrievedMessages.add(retrievedMessages.size(), retrievedMessage);
                counter--;
                if(counter <= 0) {
                    LOGGER.info("Master [" + queueName + "] has " + masterQueue.size() + " messages");
                    return retrievedMessages;
                }
            }
            LOGGER.info("Master [" + queueName + "] has " + masterQueue.size() + " messages");
        }
        return retrievedMessages;
    }

    public boolean produceMessageToSlave(final String slaveId, final Message message) throws Exception {
        validateQueueName();
        boolean containsslaveId = slaveQueues.containsKey(slaveId);
        synchronized (slaveLock) {
            if (containsslaveId) {
                boolean produced = slaveQueues.get(slaveId).add(message);
                LOGGER.info("Slave queue [" + slaveId + "] has " + slaveQueues.get(slaveId).size() + " messages");
                return produced;
            } else {
                return createSlaveQueueAndTrySendMessage(slaveId, message);
            }
        }
    }

    private boolean createSlaveQueueAndTrySendMessage(final String slaveId, final Message message) {
        Queue<Message> newSlaveQueue = new LinkedBlockingQueue<>();
        boolean wasMessageAdded = newSlaveQueue.add(message);
        LOGGER.info("Slave queue [" + slaveId + "] has " + newSlaveQueue.size() + " messages");
        if (slaveQueues.put(slaveId, newSlaveQueue) == null) {
            LOGGER.info("There wasn't a previously queue with slaveId " + slaveId);
        }
        return wasMessageAdded;
    }

    private String createSlaveQueue(final String slaveId) {
        synchronized (slaveLock) {
            if(!slaveQueues.containsKey(slaveId)) {
                Queue<Message> newSlaveQueue = new LinkedBlockingQueue<>();
                LOGGER.info("Slave queue [" + slaveId + "] has " + newSlaveQueue.size() + " messages");
                if (slaveQueues.put(slaveId, newSlaveQueue) == null) {
                    LOGGER.info("There wasn't a previously queue with slaveId " + slaveId);
                }
            }else{
                LOGGER.info("Slave queue [" + slaveId + "] already exists with [ " + slaveQueues.get(slaveId).size() + " ] messages");
            }
        }
        return slaveId;
    }

    @Override
    public boolean broadcastMessageToSlave(final String slaveIdOrigin, final Message message) throws Exception {
        validateQueueName();
        synchronized (slaveLock) {
            boolean containsslaveId = false;
            boolean broadcasted = false;
            Iterator<String> SlaveQueuesIterator = slaveQueues.keySet().iterator();
            while(SlaveQueuesIterator.hasNext()) {
                System.out.println();
                broadcasted = true;
                final String SlaveQueueName = SlaveQueuesIterator.next();
                if(slaveIdOrigin != null && slaveIdOrigin.equals(SlaveQueueName)) {
                    containsslaveId = true;
                }
                slaveQueues.get(SlaveQueueName).add(message);
            }
            if(!containsslaveId && slaveIdOrigin != null && !slaveIdOrigin.trim().isEmpty()) {
                createSlaveQueueAndTrySendMessage(slaveIdOrigin, message);
                broadcasted = true;
            }
            return broadcasted;
        }
    }

    public Message peekMessageOfSlave(final String slaveId) throws Exception {
        validateQueueName();
        Queue<Message> messageQueue = slaveQueues.get(slaveId);
        return messageQueue == null ? null : messageQueue.peek();
    }

    public Message consumeMessageOfSlave(final String slaveId) throws Exception {
        validateQueueName();
        synchronized (slaveLock) {
            boolean containsslaveId = slaveQueues.containsKey(slaveId);
            if (containsslaveId) {
                Message messagePolled = slaveQueues.get(slaveId).poll();
                LOGGER.info("Slave queue [" + slaveId + "] has " + slaveQueues.get(slaveId).size() + " messages");
                return messagePolled;
            }
        }
        throw new CloudiaMessageException("", CloudiaMessageException.SLAVE_NOT_FOUND);
    }

    public List<Message> consumeMessageOfSlave(final String slaveId, final int amount) throws Exception {
        validateQueueName();
        List<Message> retrievedMessages = new LinkedList<>();
        synchronized (slaveLock) {
            Queue<Message> retrievedSlaveQueue = slaveQueues.get(slaveId);
            if(retrievedSlaveQueue != null) {
                int counter = amount;
                while(counter > 0) {
                    Message retrievedMessage = retrievedSlaveQueue.poll();
                    if(retrievedMessage == null) {
                        LOGGER.info("Slave queue [" + slaveId + "] has " + retrievedSlaveQueue.size() + " messages");
                        return retrievedMessages;
                    }
                    retrievedMessages.add(retrievedMessages.size(), retrievedMessage);
                    counter--;
                    if(counter <= 0) {
                        LOGGER.info("Slave queue [" + slaveId + "] has " + retrievedSlaveQueue.size() + " messages");
                        return retrievedMessages;
                    }
                }
            }
            LOGGER.info("Slave queue [" + slaveId + "] has 0 messages");
            throw new CloudiaMessageException("", CloudiaMessageException.SLAVE_NOT_FOUND);
        }
    }

    @Override
    public synchronized String addSlaveMessageQueue(String slaveId) throws Exception {
        validateQueueName();
        synchronized (slaveLock) {
            String queueName = createSlaveQueue(slaveId);
            if (queueName == null) {
                throw new CloudiaMessageException("", CloudiaMessageException.SLAVE_NOT_FOUND);
            }
            return queueName;
        }
    }

}
