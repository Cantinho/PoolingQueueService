package com.cantinho.cms.sqs.impl;

import com.amazonaws.services.sqs.model.*;

import java.util.List;

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
public interface AmazonSQSApi {

    String getBaseUrl();

    String getAccountId();

    String getQueueType();

    /**
     * Gets queue url by {@param queueName}.
     * @param queueName
     * @return
     */
    String getQueueUrl(final String queueName);

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
     * Deletes messages.
     * @param queueName
     * @param messageBatchRequestEntries
     * @return
     */
    DeleteMessageBatchResult deleteMessageBatch(String queueName, List<DeleteMessageBatchRequestEntry> messageBatchRequestEntries);

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
     * Lists existing queues starting with a prefix name.
     * @return
     */
    ListQueuesResult listQueues(final String prefixName);

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
