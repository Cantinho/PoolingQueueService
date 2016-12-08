package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.util.List;

/**
 * Created by samirtf on 27/11/16.
 */
public class SimpleAmazonSQS implements AmazonSQSApi {
    @Override
    public SendMessageResult sendMessage(String queueName, Message message) {
        return false;
    }

    @Override
    public boolean sendMessage(String queueName, List<Message> messages) {
        return false;
    }

    @Override
    public List<Message> receiveMessage(String queueName, int messageAmount) {
        return null;
    }

    @Override
    public boolean deleteMessage(String queueName, Message message) {
        return false;
    }

    @Override
    public List<Boolean> deleteMessage(String queueName, List<Message> messages) {
        return null;
    }

    @Override
    public void changeMessageVisibility(String queueName, Message message) {

    }

    @Override
    public CreateQueueResult createQueue(String queueName) {
        return false;
    }

    @Override
    public ListQueuesResult listQueues() {
        return null;
    }

    @Override
    public DeleteQueueResult deleteQueue(String queueName) {
        return false;
    }

    @Override
    public void purgeQueue(String queueName) {

    }
}
