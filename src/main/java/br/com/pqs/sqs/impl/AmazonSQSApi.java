package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;

import java.util.List;

/**
 * Created by samirtf on 27/11/16.
 */
public interface AmazonSQSApi {

    /* Basic Single and Batch Message Operations */

    /**
     * Sends message to an specific queue.
     * @param queueName
     * @param message
     * @return
     */
    boolean sendMessage(String queueName, Message message);

    /**
     * Sends messages to an specific queue.
     * @param queueName
     * @param messages
     * @return
     */
    boolean sendMessage(String queueName, List<Message> messages);

    /**
     * Receives up to {@param messageAcount } messages from an specific {@param queueName } queue.
     * @param queueName
     * @param messageAmount
     * @return
     */
    List<Message> receiveMessage(String queueName, int messageAmount);

    /**
     * Deletes a previously received {@param message } from an specific queue.
     * @param queueName
     * @param message
     * @return
     */
    boolean deleteMessage(String queueName, Message message);

    /**
     * Deletes previously received {@param messages } from an specific queue.
     * @param queueName
     * @param messages
     * @return
     */
    List<Boolean> deleteMessage(String queueName, List<Message> messages);

    /**
     * Change the vibility wait time of a previously received {@param message }
     * from specific queue {@param queueName }.
     * @param queueName
     * @param message
     */
    void changeMessageVisibility(String queueName, Message message);



    /* Basic Queues Management Operations */

    /**
     * Creates a queue by {@param queueName }.
     * @param queueName
     * @return
     */
    boolean createQueue(String queueName);

    /**
     * Lists existing queues.
     * @return
     */
    List<String> listQueues();

    /**
     * Deletes a queue by {@param queueName }.
     * @param queueName
     * @return
     */
    boolean deleteQueue(String queueName);

    /**
     * Excludes all messages of an specific {@param queueName }.
     * @param queueName
     */
    void purgeQueue(String queueName);

}
