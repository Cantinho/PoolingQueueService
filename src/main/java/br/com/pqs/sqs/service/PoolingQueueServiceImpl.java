package br.com.pqs.sqs.service;

import br.com.pqs.bean.Message;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.SimpleMessageQueue;
import br.com.pqs.sqs.model.MessageMapper;
import br.com.processor.IMessageProcessor;
import br.com.processor.SimpleMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jordaoesa on 06/12/16.
 */
@Component
public class PoolingQueueServiceImpl implements PoolingQueueService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SimpleMessageQueue simpleMessageQueue;

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
    public MessageMapper cconn(String serialNumber, String contentType, MessageMapper messageMapper) {
        MessageMapper returnMessage = new MessageMapper();
        System.out.println("MESSAGE MAPPER.GETMSG()" + messageMapper.getMsg());
        boolean connected = tryingToCreateCentral(serialNumber);

        returnMessage.setTp(1);
        System.out.println("CCONN -MSG WRAPPER :" + messageMapper.getMsg());
        returnMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), connected));
        return returnMessage;
    }

    @Override
    public MessageMapper cpull(String serialNumber) {

        Message message = null;
        try {
            message = simpleMessageQueue.consumeMessageOfCentral(serialNumber);
        }  catch (PoolingQueueException e) {
            LOGGER.warn("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
            if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                tryingToCreateCentral(serialNumber);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        MessageMapper responseMessage = new MessageMapper();
        if(message == null){
            responseMessage.setMsg("");
        } else {
            responseMessage.setMsg(message.getMessage());
        }

        return responseMessage;
    }

    @Override
    public MessageMapper cpush(String serialNumber, String applicationID, String broadcast, String contentType, MessageMapper messageMapper) {

        MessageMapper responseMessage = new MessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, applicationID, timestamp, priority, messageMapper.getMsg());

        if(broadcast != null) { // post message to all applications
            boolean broadcasted = false;
            try {
                broadcasted = simpleMessageQueue.broadcastMessageToApplication(serialNumber, applicationID, message);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            responseMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), broadcasted));
            System.out.println("TP 1" + responseMessage);
            return responseMessage;
        } else { // post message to single applicationID
            boolean produced = false;
            try {
                produced = simpleMessageQueue.produceMessageToApplication(serialNumber, applicationID, message);
                responseMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), produced));
                System.out.println("TP 2" + responseMessage);
                return responseMessage;
            } catch (PoolingQueueException e) {
                LOGGER.error(e.getMessage());
                LOGGER.warn("Unable to produce an application message to a nonexistent central [" + serialNumber + "].");
                if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                    try {
                        tryingToCreateCentral(serialNumber);
                        produced = simpleMessageQueue.produceMessageToApplication(serialNumber, applicationID, message);
                        responseMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), produced));
                        System.out.println("TP 3" + responseMessage);
                        return responseMessage;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

            responseMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), produced));
            System.out.println("TP 4" + responseMessage);
            return responseMessage;
        }
    }

    @Override
    public MessageMapper aconn(String serialNumber, String applicationID, String contentType, MessageMapper message) {

        //TODO: should we try to create a queue to app?
        //TODO Should I? Really?
        //TODO Request STATUS
        MessageMapper returnMessage = new MessageMapper();

        try {
            simpleMessageQueue.addApplicationPoolingQueue(serialNumber, applicationID);
            returnMessage.setMsg(iMessageProcessor.getStatusMessage(message.getMsg(), true));
        } catch (Exception e) {
            returnMessage.setMsg(iMessageProcessor.getStatusMessage(message.getMsg(), false));
            e.printStackTrace();
        }
        return returnMessage;
    }

    @Override
    public MessageMapper apull(String serialNumber, String applicationID, String messageAmount) {

        MessageMapper responseMessage = new MessageMapper();

        if(messageAmount == null || Integer.valueOf(messageAmount) == 0 || Integer.valueOf(messageAmount) == 1) {
            LOGGER.info("pulling only 1 message");
            Message message = null;
            try {
                message = simpleMessageQueue.consumeMessageOfApplication(serialNumber, applicationID);
            } catch (PoolingQueueException e) {
                if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                    LOGGER.warn("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
                    tryingToCreateCentral(serialNumber);
                    try {
                        message = simpleMessageQueue.consumeMessageOfApplication(serialNumber, applicationID);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        LOGGER.error(e1.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }

            if(message == null){
                responseMessage.setMsg("");
            } else {
                responseMessage.setMsg(iMessageProcessor.getStatusMessage(message.getMessage(), true));
            }
            return responseMessage;
        } else {
            LOGGER.info("pulling " + messageAmount + " messages");
            List<Message> messages = null;
            try {
                messages = simpleMessageQueue.consumeMessageOfApplication(serialNumber, applicationID, Integer.valueOf(messageAmount));
            } catch (PoolingQueueException e) {
                //e.printStackTrace();
                LOGGER.warn("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
                if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                    tryingToCreateCentral(serialNumber);
                    try {
                        messages = simpleMessageQueue.consumeMessageOfApplication(serialNumber, applicationID, Integer.valueOf(messageAmount));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> msgs = new LinkedList<>();
            if(messages != null){
                for(Message msg : messages)
                    msgs.add(msg.getMessage());
            }

            responseMessage.setMsgs(msgs);
            return responseMessage;
        }
    }

    @Override
    public MessageMapper apush(String serialNumber, String applicationID, String contentType, MessageMapper messageMapper) {

        MessageMapper responseMessage = new MessageMapper();

        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, applicationID, timestamp, priority, messageMapper.getMsg());
        boolean produced = false;
        try {
            produced = simpleMessageQueue.produceMessageToCentral(serialNumber, message);
        } catch (PoolingQueueException e) {
            LOGGER.warn("Unable to produce an application message from a nonexistent central [" + serialNumber + "].");
            if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                tryingToCreateCentral(serialNumber);
                try {
                    produced = simpleMessageQueue.produceMessageToCentral(serialNumber, message);
                } catch (Exception e1) {
                    LOGGER.error(e1.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        responseMessage.setMsg(iMessageProcessor.getStatusMessage(messageMapper.getMsg(), produced));
        return responseMessage;
    }

    private boolean tryingToCreateCentral(final String serialNumber) {
        try {
            LOGGER.info("Trying to create a central [" + serialNumber + "].");
            simpleMessageQueue.createPoolingQueue(serialNumber);
            return true;
        } catch (Exception e1) {
            LOGGER.error("It failed miserably in creating a new central [" + serialNumber + "].");
            e1.printStackTrace();
            return false;
        }
    }

}
