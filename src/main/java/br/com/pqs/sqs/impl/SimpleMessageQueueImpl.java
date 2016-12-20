package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.SimpleMessageQueue;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static br.com.pqs.exceptions.PoolingQueueException.MASTER_NOT_FOUND;

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
public class SimpleMessageQueueImpl implements SimpleMessageQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(SimpleMessageQueueImpl.class);

    private final String poolingQueueLock = "POOLING_QUEUE_MAP_LOCK";
    private Map<String, IPoolingQueue> poolingQueueMap = new TreeMap<>();
    private String poolingQueueClassName;

    public SimpleMessageQueueImpl(){}

    public SimpleMessageQueueImpl(final String className){
        this.poolingQueueClassName = className;
    }

    public synchronized void setPoolingQueueClassName(final String poolingQueueClassName) {
        this.poolingQueueClassName = poolingQueueClassName;
    }

    public synchronized void createPoolingQueue(final String queueName) throws Exception {
        if(poolingQueueMap.containsKey(queueName)) {
            LOGGER.info("PoolingQueue already exists [" + queueName + "].");
            return;
        }
        if(poolingQueueClassName != null && !poolingQueueClassName.trim().isEmpty()) {
          if(poolingQueueClassName.equals(AmazonPoolingQueue.class.getName())) {
              LOGGER.info("SimpleMessageQueueImpl instance creating a AmazonPoolingQueue.");
              AmazonPoolingQueue amazonPoolingQueue = new AmazonPoolingQueue();
              AmazonSQSApi amazonSQSApi = null;
              if(amazonSQSApi == null) {
                  throw new NotImplementedException("Implement this!");
              }
              amazonPoolingQueue.setAmazonSQSApi(amazonSQSApi);
              amazonPoolingQueue.setQueueName(queueName);
              poolingQueueMap.put(queueName, amazonPoolingQueue);
          }
        }

        LOGGER.info("SimpleMessageQueueImpl instance creating a PoolingQueue [" + queueName + "].");
        PoolingQueue poolingQueue = new PoolingQueue(queueName);
        poolingQueueMap.put(queueName, poolingQueue);
    }

    public boolean produceMessageToMaster(final String masterName, final Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).produceMessageToMaster(message);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public Message peekMessageOfMaster(final String masterName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).peekMessageOfMaster();
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public Message consumeMessageOfMaster(final String masterName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).consumeMessageOfMaster();
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public List<Message> consumeMessageOfMaster(final String masterName, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).consumeMessageOfMaster(amount);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }



    public boolean produceMessageToSlave(final String masterName, final String slaveId, final Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).produceMessageToSlave(slaveId, message);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public synchronized boolean broadcastMessageToSlave(String masterName, String slaveId, Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).broadcastMessageToSlave(slaveId, message);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public synchronized Message peekMessageOfSlave(final String masterName, final String slaveId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).peekMessageOfSlave(slaveId);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public synchronized Message consumeMessageOfSlave(final String masterName, final String slaceId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).consumeMessageOfSlave(slaceId);
            }
        }
        LOGGER.info("Master [" + masterName + "] does not exist.");
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    public List<Message> consumeMessageOfSlave(final String masterName, final String slaveId, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).consumeMessageOfSlave(slaveId, amount);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public String addSlavePoolingQueue(final String masterName, final String slaveId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(masterName)) {
                return poolingQueueMap.get(masterName).addSlavePoolingQueue(slaveId);
            }
        }
        throw new PoolingQueueException("Master [" + masterName + "] does not exist.", MASTER_NOT_FOUND);
    }

    @Override
    public List<String> listPoolingQueues() {
        synchronized (poolingQueueLock) {
            List<String> queueNames = new ArrayList<>();
            Iterator<String> poolingQueueNames = poolingQueueMap.keySet().iterator();
            while(poolingQueueNames.hasNext()) {
                queueNames.add(poolingQueueNames.next());
            }
            return queueNames;
        }
    }

}
