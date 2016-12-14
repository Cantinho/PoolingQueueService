package br.com.pqs.sqs;

import br.com.pqs.bean.Message;
import br.com.pqs.exceptions.PoolingQueueException;

import java.util.List;

/**
 * Created by jordaoesa on 25/11/16.
 */
public interface SimpleMessageQueue {

    void createPoolingQueue(final String queueName) throws Exception;

    void setPoolingQueueClassName(final String poolingQueueClassName);

    boolean produceMessageToCentral(final String centralName, final Message message) throws Exception;

    Message peekMessageOfCentral(final String centralName) throws Exception;

    Message consumeMessageOfCentral(final String centralName) throws Exception;

    List<Message> consumeMessageOfCentral(final String centralName, final int amount) throws Exception;

    boolean produceMessageToApplication(final String centralName, final String applicationId, Message message) throws Exception;

    boolean broadcastMessageToApplication(String centralName, String applicationId, Message message) throws Exception;

    Message peekMessageOfApplication(final String centralName, final String applicationId) throws Exception;

    Message consumeMessageOfApplication(final String centralName, final String applicationId) throws Exception;

    List<Message> consumeMessageOfApplication(final String centralName, final String applicationId, final int amount) throws Exception;

    String addApplicationPoolingQueue(String serialNumber, String applicationID) throws Exception;

    List<String> listPoolingQueues();

}
