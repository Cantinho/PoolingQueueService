package br.com.example.sqs.impl;

import br.com.example.bean.Message;

import java.util.List;

/**
 * Created by samirtf on 27/11/16.
 */
public class SimpleAmazonSQS implements AmazonSQSApi {
    @Override
    public boolean sendMessage(String queueName, Message message) {
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
    public boolean createQueue(String queueName) {
        return false;
    }

    @Override
    public List<String> listQueues() {
        return null;
    }

    @Override
    public boolean deleteQueue(String queueName) {
        return false;
    }

    @Override
    public void purgeQueue(String queueName) {

    }
}
