package br.com.pqs.sqs.impl;

import com.amazonaws.services.sqs.model.*;

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
    SendMessageResult sendMessage(String queueName, String message);

    /**
     * Sends messages to an specific queue.
     * @param queueName
     * @param messages
     * @return
     */
    boolean sendMessage(String queueName, List<String> messages);

    /**
     * Receives up to {@param messageAcount } messages from an specific {@param queueName } queue.
     * @param queueName
     * @param messageAmount
     * @return
     */
    List<Message> receiveMessage(String queueName, int messageAmount);

    /**
     * Receives messages from an specific {@param receiveMessageRequest } queue.
     * @param receiveMessageRequest
     * @return
     */
    List<Message> receiveMessage(ReceiveMessageRequest receiveMessageRequest);

    /**
     * Deletes a previously received {@param message } from an specific queue.
     * @param queueName
     * @param message
     * @return
     */
    DeleteMessageResult deleteMessage(String queueName, String message);

    /**
     * Deletes previously received {@param deleteMessageRequest } messages..
     * @param deleteMessageRequest
     * @return
     */
    public DeleteMessageResult deleteMessage(DeleteMessageRequest deleteMessageRequest);

    /**
     * Change the visibility wait time {@param visibilityTimeout } of a previously received {@param receiptHandle }
     * from specific queue {@param queueName }.
     * @param queueName
     * @param receiptHandle
     * @param visibilityTimeout
     */
    ChangeMessageVisibilityResult changeMessageVisibility(String queueName, String receiptHandle,
        final Integer visibilityTimeout);



    /* Basic Queues Management Operations */

    /**
     * Creates a queue by {@param queueName }.
     * @param queueName
     * @return
     */
    CreateQueueResult createQueue(String queueName);

    /**
     * Creates a queue by {@param queueName }.
     * @param createQueueRequest
     * @return
     */
    CreateQueueResult createQueue(CreateQueueRequest createQueueRequest);

    /**
     * Lists existing queues.
     * @return
     */
    ListQueuesResult listQueues();

    /**
     * Deletes a queue by {@param queueName }.
     * @param queueName
     * @return
     */
    DeleteQueueResult deleteQueue(String queueName);

    /**
     * Deletes a queue by {@param deleteQueueRequest }.
     * @param deleteQueueRequest
     * @return
     */
    DeleteQueueResult deleteQueue(DeleteQueueRequest deleteQueueRequest);

    /**
     * Excludes all messages of an specific {@param purgeQueueRequest } queue.
     * @param purgeQueueRequest
     */
    PurgeQueueResult purgeQueue(PurgeQueueRequest purgeQueueRequest);

}
