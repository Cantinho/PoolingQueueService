package com.cantinho.cms.sqs.impl;

import com.cantinho.cms.bean.Message;
import com.cantinho.cms.exceptions.CloudiaMessageException;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.cantinho.cms.exceptions.CloudiaMessageException.MASTER_NOT_FOUND;

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
@Component
public class MessageQueueImpl implements com.cantinho.cms.sqs.MessageQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(MessageQueueImpl.class);

    private final String messageQueueLock = "MESSAGE_QUEUE_MAP_LOCK";
    private Map<String, IMessageQueue> messageQueueMap = new TreeMap<>();
    private String messageQueueClassName;

    public MessageQueueImpl(){}

    public MessageQueueImpl(final String className){
        this.messageQueueClassName = className;
    }

    public synchronized void setMessageQueueClassName(final String messageQueueClassName) {
        this.messageQueueClassName = messageQueueClassName;
    }

    public synchronized void createMessageQueue(final String queueName) throws Exception {
        if(messageQueueMap.containsKey(queueName)) {
            LOGGER.info("MessageQueue already exists [" + queueName + "].");
            return;
        }
        if(messageQueueClassName != null && !messageQueueClassName.trim().isEmpty()) {
          if(messageQueueClassName.equals(AmazonMessageQueue.class.getName())) {
              LOGGER.info("MessageQueueImpl instance creating a AmazonMessageQueue.");
              AmazonMessageQueue amazonMessageQueue = new AmazonMessageQueue();
              AmazonSQSApi amazonSQSApi = null;
              if(amazonSQSApi == null) {
                  throw new NotImplementedException("Implement this!");
              }
              amazonMessageQueue.setAmazonSQSApi(amazonSQSApi);
              amazonMessageQueue.setQueueName(queueName);
              messageQueueMap.put(queueName, amazonMessageQueue);
          }
        }

        LOGGER.info("MessageQueueImpl instance creating a MessageQueue [" + queueName + "].");
        MessageQueue messageQueue = new MessageQueue(queueName);
        messageQueueMap.put(queueName, messageQueue);
    }

    public boolean produceMessageToMaster(final String masterId, final Message message) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).produceMessageToMaster(message);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public Message peekMessageOfMaster(final String masterId) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).peekMessageOfMaster();
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public Message consumeMessageOfMaster(final String masterId) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).consumeMessageOfMaster();
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public List<Message> consumeMessageOfMaster(final String masterId, final int amount) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).consumeMessageOfMaster(amount);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }



    public boolean produceMessageToSlave(final String masterId, final String slaveId, final Message message) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).produceMessageToSlave(slaveId, message);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public synchronized boolean broadcastMessageToSlave(String masterId, String slaveId, Message message) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).broadcastMessageToSlave(slaveId, message);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public synchronized Message peekMessageOfSlave(final String masterId, final String slaveId) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).peekMessageOfSlave(slaveId);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public synchronized Message consumeMessageOfSlave(final String masterId, final String slaceId) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).consumeMessageOfSlave(slaceId);
            }
        }
        LOGGER.info("Master [" + masterId + "] does not exist.");
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    public List<Message> consumeMessageOfSlave(final String masterId, final String slaveId, final int amount) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).consumeMessageOfSlave(slaveId, amount);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public String addSlaveMessageQueue(final String masterId, final String slaveId) throws Exception {
        synchronized (messageQueueLock) {
            if(messageQueueMap.containsKey(masterId)) {
                return messageQueueMap.get(masterId).addSlaveMessageQueue(slaveId);
            }
        }
        throw new CloudiaMessageException("Master [" + masterId + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public List<String> listMessageQueues() {
        synchronized (messageQueueLock) {
            List<String> queueNames = new ArrayList<>();
            Iterator<String> messageQueueNames = messageQueueMap.keySet().iterator();
            while(messageQueueNames.hasNext()) {
                queueNames.add(messageQueueNames.next());
            }
            return queueNames;
        }
    }

}
