package br.com.example.sqs.impl;

import br.com.example.bean.Message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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

}
