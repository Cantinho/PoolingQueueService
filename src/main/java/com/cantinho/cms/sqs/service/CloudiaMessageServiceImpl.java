package com.cantinho.cms.sqs.service;

import br.com.processor.mapper.MessageMapper;
import com.cantinho.cms.bean.Message;
import com.cantinho.cms.bean.CMSResponse;
import com.cantinho.cms.exceptions.CloudiaMessageException;
import com.cantinho.cms.sqs.MessageQueue;
import br.com.processor.IMessageProcessor;
import br.com.processor.SimpleMessageProcessor;
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
public class CloudiaMessageServiceImpl implements CloudiaMessageService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageQueue messageQueue;

    @Value("${tp}")
    private Integer tp;

    private IMessageProcessor iMessageProcessor;

    @PostConstruct
    void init() {
        System.out.println("CloudiaMessageServiceImpl - POST CONSTRUCT");
        iMessageProcessor = new SimpleMessageProcessor();
    }

    @Override
    public void setIMessageProcessor(IMessageProcessor iMessageProcessor) throws CloudiaMessageException {
        if(iMessageProcessor == null) {
            throw new CloudiaMessageException("iMessageProcessor must be not null");
        }
        this.iMessageProcessor = iMessageProcessor;
    }

    @Override
    public MessageMapper mconn(String masterSN, String contentType, MessageMapper messageMapper) {
        MessageMapper returnMessage = new MessageMapper();
        System.out.println("MESSAGE MAPPER.getMessage()" + messageMapper.getMessage());
        boolean connected = tryingToCreateMaster(masterSN);

        returnMessage.setIndex(1);
        System.out.println("CCONN -MSG WRAPPER :" + messageMapper.getMessage());
        returnMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), connected));
        return returnMessage;
    }

    @Override
    public boolean misconn(String masterId) {
        return isMasterConnected(masterId);
    }

    @Override
    public boolean sisconn(String slaveId) {
        return false;
    }

    @Override
    public CMSResponse mpull(final String masterSN) {

        long initTime = new Date().getTime();
        long nowTime;
        int offset = new Random().nextInt(10);

        Message message = null;
        try {
            while(message == null) {

                message = messageQueue.consumeMessageOfMaster(masterSN);
                //System.out.println("GETTING MESSAGE: " + message);
                Thread.sleep(100 + new Random().nextInt(100));

                nowTime = new Date().getTime();
                if(nowTime - initTime >= ((tp + offset)*1000)){
                    break;
                }

            }
        }  catch (CloudiaMessageException e) {
            LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterSN + "].");
            if(e.getCode() == CloudiaMessageException.MASTER_NOT_FOUND) {
                tryingToCreateMaster(masterSN);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        System.out.println("Abortando pulling de forma segura!");
        return getResponseFromMessage(message);
    }

    @Override
    public MessageMapper mpush(final String masterId, final String slaveId, final String broadcast, final String contentType, final MessageMapper messageMapper) {

        System.out.println("TESTE mpush: " + messageMapper.getMessage());

        MessageMapper responseMessage = new MessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(masterId, slaveId, timestamp, priority, messageMapper.getMessage());

        if(broadcast != null) { // post message to all slaves
            boolean broadcasted = false;
            try {
                broadcasted = messageQueue.broadcastMessageToSlave(masterId, slaveId, message);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), broadcasted));
            System.out.println("TP 1" + responseMessage);
            return responseMessage;
        } else { // post message to single slaveId
            boolean produced = false;
            try {
                produced = messageQueue.produceMessageToSlave(masterId, slaveId, message);
                responseMessage.setMessage(iMessageProcessor.getStatusMessage(messageMapper.getMessage(), produced));
                System.out.println("TP 2" + responseMessage);
                return responseMessage;
            } catch (CloudiaMessageException e) {
                LOGGER.error(e.getMessage());
                LOGGER.warn("Unable to produce an slave message to a nonexistent master [" + masterId + "].");
                if(e.getCode() == CloudiaMessageException.MASTER_NOT_FOUND) {
                    try {
                        tryingToCreateMaster(masterId);
                        produced = messageQueue.produceMessageToSlave(masterId, slaveId, message);
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
    public MessageMapper sconn(String masterId, String slaveId, String contentType, MessageMapper message) {

        //TODO: should we try to create a queue to app?
        //TODO Should I? Really?
        //TODO Request STATUS
        MessageMapper returnMessage = new MessageMapper();

        try {
            messageQueue.addSlaveMessageQueue(masterId, slaveId);
            returnMessage.setMessage(iMessageProcessor.getStatusMessage(message.getMessage(), true));
        } catch (Exception e) {
            returnMessage.setMessage(iMessageProcessor.getStatusMessage(message.getMessage(), false));
            e.printStackTrace();
        }
        return returnMessage;
    }

    @Override
    public CMSResponse spull(String masterId, String slaveId, String messageAmount) {

        if(messageAmount == null || Integer.valueOf(messageAmount) == 0 || Integer.valueOf(messageAmount) == 1) {
            LOGGER.info("pulling only 1 message");
            Message message = null;
            try {
                message = messageQueue.consumeMessageOfSlave(masterId, slaveId);
            } catch (CloudiaMessageException e) {
                if(e.getCode() == CloudiaMessageException.MASTER_NOT_FOUND) {
                    LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterId + "].");
                    tryingToCreateMaster(masterId);
                    try {
                        message = messageQueue.consumeMessageOfSlave(masterId, slaveId);
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
                messages = messageQueue.consumeMessageOfSlave(masterId, slaveId, Integer.valueOf(messageAmount));
            } catch (CloudiaMessageException e) {
                //e.printStackTrace();
                LOGGER.warn("Unable to consume an slave message from a nonexistent master [" + masterId + "].");
                if(e.getCode() == CloudiaMessageException.MASTER_NOT_FOUND) {
                    tryingToCreateMaster(masterId);
                    try {
                        messages = messageQueue.consumeMessageOfSlave(masterId, slaveId, Integer.valueOf(messageAmount));
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
    public MessageMapper spush(String masterId, String slaveId, String contentType, MessageMapper messageMapper) {

        MessageMapper responseMessage = new MessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(masterId, slaveId, timestamp, priority, messageMapper.getMessage());
        boolean produced = false;
        try {
            produced = messageQueue.produceMessageToMaster(masterId, message);
        } catch (CloudiaMessageException e) {
            LOGGER.warn("Unable to produce an slave message from a nonexistent master [" + masterId + "].");
            if(e.getCode() == CloudiaMessageException.MASTER_NOT_FOUND) {
                tryingToCreateMaster(masterId);
                try {
                    produced = messageQueue.produceMessageToMaster(masterId, message);
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
            messageQueue.createMessageQueue(masterSN);
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
        List<String> masterQueues = messageQueue.listMessageQueues();
        return masterQueues.contains(masterSN);
    }

    private CMSResponse getResponseFromMessage(Message message) {

        MessageMapper body = new MessageMapper();
        HttpHeaders headers = new HttpHeaders();

        if(message == null){
            body.setMessage("");
        } else {
            if(message.getMessage() != null) {
                body.setMessage(message.getMessage());
            }
            if (message.getSenderId() != null) {
                headers.set("Master-ID", message.getSenderId());
            }
            if (message.getReceiverId() != null) {
                headers.set("Slave-ID", message.getReceiverId());
            }
        }

        return new CMSResponse(headers, body);
    }

    private CMSResponse getResponseFromMessages(List<Message> messages) {
        MessageMapper body = new MessageMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        if(messages == null || messages.isEmpty()){
            body.setMessages(new ArrayList<>());
        } else {
            Message message = messages.get(0);
            if (message != null && message.getSenderId() != null) {
                headers.set("Master-ID", message.getSenderId());
            }
            if (message != null && message.getReceiverId() != null) {
                headers.set("Slave-ID", message.getReceiverId());
            }

            List<String> messagesStr = new LinkedList<>();
            for(Message msg : messages){
                messagesStr.add(msg.getMessage());
            }
            body.setMessages(messagesStr);
        }

        return new CMSResponse(headers, body);
    }

}
