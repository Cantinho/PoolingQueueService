package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;

import java.util.List;

/**
 * Created by samirtf on 27/11/16.
 */
public interface IPoolingQueue {

    boolean produceMessageToCentral(final Message message) throws Exception;

    Message peekMessageOfCentral() throws Exception;

    Message consumeMessageOfCentral() throws Exception;

    List<Message> consumeMessageOfCentral(final int amount) throws Exception;

    boolean produceMessageToApplication(final String applicationId, final Message message) throws Exception;

    boolean broadcastMessageToApplication(final String applicationIdOrigin, final Message message) throws Exception;

    Message peekMessageOfApplication(final String applicationId) throws Exception;

    Message consumeMessageOfApplication(final String applicationId) throws Exception;

    List<Message> consumeMessageOfApplication(final String applicationId, final int amount) throws Exception;

    String addApplicationPoolingQueue(String applicationID) throws Exception;
}
