package br.com.pqs.sqs.service;

import br.com.pqs.bean.Message;
import br.com.pqs.bean.PQSResponse;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.SimpleMessageQueue;
import br.com.processor.IMessageProcessor;
import br.com.processor.SimpleMessageProcessor;
import br.com.processor.mapper.SimpleMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

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
public class PoolingQueueServiceImpl implements PoolingQueueService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SimpleMessageQueue simpleMessageQueue;

    @Value("${tp}")
    private Integer tp;

    private IMessageProcessor iMessageProcessor;

    @PostConstruct
    void init() {
        System.out.println("PoolingQueueServiceImpl - POST CONSTRUCT");
        iMessageProcessor = new SimpleMessageProcessor();
    }

    @Override
    public void setIMessageProcessor(IMessageProcessor iMessageProcessor) throws PoolingQueueException {
        if(iMessageProcessor == null) {
            throw new PoolingQueueException("iMessageProcessor must be not null");
        }
        this.iMessageProcessor = iMessageProcessor;
    }

    @Override
    public SimpleMessageMapper mconn(String masterSN, String contentType, SimpleMessageMapper messageMapper) {
        SimpleMessageMapper returnMessage = new SimpleMessageMapper();
        System.out.println("MESSAGE MAPPER.getMessage()" + messageMapper.getMessage());
        boolean connected = tryingToCreateMaster(masterSN);

        returnMessage.setIndex(1);
        System.out.println("CCONN -MSG WRAPPER :" + messageMapper.getMessage());
        returnMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), connected));
        return returnMessage;
    }

    @Override
    public boolean isconn(String masterSN) {
        return isMasterConnected(masterSN);
    }

    @Override
    public PQSResponse mpull(final String masterSN) {

        long initTime = new Date().getTime();
        long nowTime;
        int offset = new Random().nextInt(10);

        Message message = null;
        try {
            while(message == null) {

                message = simpleMessageQueue.consumeMessageOfMaster(masterSN);
                //System.out.println("GETTING MESSAGE: " + message);
                Thread.sleep(100 + new Random().nextInt(100));

                nowTime = new Date().getTime();
                if(nowTime - initTime >= ((tp + offset)*1000)){
                    break;
                }

            }
        }  catch (PoolingQueueException e) {
            LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterSN + "].");
            if(e.getCode() == PoolingQueueException.MASTER_NOT_FOUND) {
                tryingToCreateMaster(masterSN);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        System.out.println("Abortando pulling de forma segura!");
        return getResponseFromMessage(message);
    }

    @Override
    public SimpleMessageMapper mpush(final String masterSN, final String slaveId, final String broadcast, final String contentType, final SimpleMessageMapper messageMapper) {

        System.out.println("TESTE mpush: " + messageMapper.getMessage());

        SimpleMessageMapper responseMessage = new SimpleMessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(masterSN, slaveId, timestamp, priority, messageMapper.getMessage());

        if(broadcast != null) { // post message to all slaves
            boolean broadcasted = false;
            try {
                broadcasted = simpleMessageQueue.broadcastMessageToSlave(masterSN, slaveId, message);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), broadcasted));
            System.out.println("TP 1" + responseMessage);
            return responseMessage;
        } else { // post message to single slaveId
            boolean produced = false;
            try {
                produced = simpleMessageQueue.produceMessageToSlave(masterSN, slaveId, message);
                responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), produced));
                System.out.println("TP 2" + responseMessage);
                return responseMessage;
            } catch (PoolingQueueException e) {
                LOGGER.error(e.getMessage());
                LOGGER.warn("Unable to produce an slave message to a nonexistent master [" + masterSN + "].");
                if(e.getCode() == PoolingQueueException.MASTER_NOT_FOUND) {
                    try {
                        tryingToCreateMaster(masterSN);
                        produced = simpleMessageQueue.produceMessageToSlave(masterSN, slaveId, message);
                        responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), produced));
                        System.out.println("TP 3" + responseMessage);
                        return responseMessage;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

            responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), produced));
            System.out.println("TP 4" + responseMessage);
            return responseMessage;
        }
    }

    @Override
    public SimpleMessageMapper sconn(String masterSN, String slaveId, String contentType, SimpleMessageMapper message) {

        //TODO: should we try to create a queue to app?
        //TODO Should I? Really?
        //TODO Request STATUS
        SimpleMessageMapper returnMessage = new SimpleMessageMapper();

        try {
            simpleMessageQueue.addSlavePoolingQueue(masterSN, slaveId);
            returnMessage.setMessage(iMessageProcessor.getStatusMessage(message.getMessage(), true));
        } catch (Exception e) {
            returnMessage.setMessage(iMessageProcessor.getStatusMessage(message.getMessage(), false));
            e.printStackTrace();
        }
        return returnMessage;
    }

    @Override
    public PQSResponse spull(String masterSN, String slaveId, String messageAmount) {

        if(messageAmount == null || Integer.valueOf(messageAmount) == 0 || Integer.valueOf(messageAmount) == 1) {
            LOGGER.info("pulling only 1 message");
            Message message = null;
            try {
                message = simpleMessageQueue.consumeMessageOfSlave(masterSN, slaveId);
            } catch (PoolingQueueException e) {
                if(e.getCode() == PoolingQueueException.MASTER_NOT_FOUND) {
                    LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterSN + "].");
                    tryingToCreateMaster(masterSN);
                    try {
                        message = simpleMessageQueue.consumeMessageOfSlave(masterSN, slaveId);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        LOGGER.error(e1.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }

            return getResponseFromMessage(message);
        } else {
            LOGGER.info("pulling " + messageAmount + " messages");
            List<Message> messages = null;
            try {
                messages = simpleMessageQueue.consumeMessageOfSlave(masterSN, slaveId, Integer.valueOf(messageAmount));
            } catch (PoolingQueueException e) {
                //e.printStackTrace();
                LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterSN + "].");
                if(e.getCode() == PoolingQueueException.MASTER_NOT_FOUND) {
                    tryingToCreateMaster(masterSN);
                    try {
                        messages = simpleMessageQueue.consumeMessageOfSlave(masterSN, slaveId, Integer.valueOf(messageAmount));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return getResponseFromMessages(messages);
        }
    }

    @Override
    public SimpleMessageMapper spush(String masterSN, String slaveId, String contentType, SimpleMessageMapper messageMapper) {

        SimpleMessageMapper responseMessage = new SimpleMessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(masterSN, slaveId, timestamp, priority, messageMapper.getMessage());
        boolean produced = false;
        try {
            produced = simpleMessageQueue.produceMessageToMaster(masterSN, message);
        } catch (PoolingQueueException e) {
            LOGGER.warn("Unable to produce an slave message from a nonexistent master [" + masterSN + "].");
            if(e.getCode() == PoolingQueueException.MASTER_NOT_FOUND) {
                tryingToCreateMaster(masterSN);
                try {
                    produced = simpleMessageQueue.produceMessageToMaster(masterSN, message);
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), produced));
        return responseMessage;
    }

    private boolean tryingToCreateMaster(final String masterSN) {
        try {
            LOGGER.info("Trying to create a master [" + masterSN + "].");
            simpleMessageQueue.createPoolingQueue(masterSN);
            return true;
        } catch (Exception e1) {
            LOGGER.error("It failed miserably in creating a new master [" + masterSN + "].");
            e1.printStackTrace();
            return false;
        }
    }

    private boolean isMasterConnected(String masterSN) {
        if(masterSN == null || masterSN.trim().isEmpty()) {
            return false;
        }
        List<String> masterQueues = simpleMessageQueue.listPoolingQueues();
        return masterQueues.contains(masterSN);
    }

    private PQSResponse getResponseFromMessage(Message message) {

        SimpleMessageMapper body = new SimpleMessageMapper();
        HttpHeaders headers = new HttpHeaders();

        if(message == null){
            body.setMessage("");
        } else {
            if(message.getMessage() != null) {
                body.setMessage(message.getMessage());
            }
            if (message.getMasterSN() != null) {
                headers.set("MasterS-N", message.getMasterSN());
            }
            if (message.getSlaveID() != null) {
                headers.set("Slave-ID", message.getSlaveID());
            }
        }

        return new PQSResponse(headers, body);
    }

    private PQSResponse getResponseFromMessages(List<Message> messages) {
        SimpleMessageMapper body = new SimpleMessageMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        if(messages == null || messages.isEmpty()){
            body.setMessages(new ArrayList<>());
        } else {
            Message message = messages.get(0);
            if (message != null && message.getMasterSN() != null) {
                headers.set("Master-SN", message.getMasterSN());
            }
            if (message != null && message.getSlaveID() != null) {
                headers.set("Slave-ID", message.getSlaveID());
            }

            List<String> messagesStr = new LinkedList<>();
            for(Message msg : messages){
                messagesStr.add(msg.getMessage());
            }
            body.setMessages(messagesStr);
        }

        return new PQSResponse(headers, body);
    }

}
