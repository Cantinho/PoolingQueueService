package br.com.example.sqs.impl;

import br.com.example.bean.Message;
import br.com.example.sqs.SimpleMessageQueue;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by jordaoesa on 25/11/16.
 */
@Component
public class SimpleMessageQueueImpl implements SimpleMessageQueue {

    private final Logger LOGGER = LoggerFactory.getLogger(SimpleMessageQueueImpl.class);

    private final String poolingQueueLock = "POOLING_QUEUE_MAP_LOCK";
    private Map<String, IPoolingQueue> poolingQueueMap = new TreeMap<>();
    private String poolingQueueClassName;

    public SimpleMessageQueueImpl(){}

    public SimpleMessageQueueImpl(final String className){
        this.poolingQueueClassName = className;
    }

    public void setPoolingQueueClassName(final String poolingQueueClassName) {
        this.poolingQueueClassName = poolingQueueClassName;
    }

    public IPoolingQueue createPoolingQueue(final String queueName) throws Exception {
        if(poolingQueueClassName != null && !poolingQueueClassName.trim().isEmpty()) {
          if(poolingQueueClassName.equals(AmazonPoolingQueue.class.getName())) {
              LOGGER.info("SimpleMessageQueueImpl instance creating a AmazonPoolingQueue.");
              AmazonPoolingQueue amazonPoolingQueue = new AmazonPoolingQueue();
              AmazonSQSApi amazonSQSApi = null;
              if(amazonSQSApi == null) {
                  throw new NotImplementedException("Implement this!");
              }
              amazonPoolingQueue.setAmazonSQSApi(amazonSQSApi);
              amazonPoolingQueue.setQueueName(queueName);
              return amazonPoolingQueue;
          }
        }

        LOGGER.info("SimpleMessageQueueImpl instance creating a PoolingQueue.");
        PoolingQueue poolingQueue = new PoolingQueue(queueName);
        return poolingQueue;
    }

    public boolean produceMessageToCentral(final String centralName, final Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).produceMessageToCentral(message);
            }
        }
        return false;
    }

    public Message peekMessageOfCentral(final String centralName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).peekMessageOfCentral();
            }
        }
        return null;
    }

    public Message consumeMessageOfCentral(final String centralName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfCentral();
            }
        }
        return null;
    }

    public List<Message> consumeMessageOfCentral(final String centralName, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfCentral(amount);
            }
        }
        return new ArrayList<>();
    }



    public boolean produceMessageToApplication(final String centralName, final String applicationId, Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).produceMessageToApplication(applicationId, message);
            }
        }
        return false;
    }

    public Message peekMessageOfApplication(final String centralName, final String applicationId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).peekMessageOfApplication(applicationId);
            }
        }
        return null;
    }

    public Message consumeMessageOfApplication(final String centralName, final String applicationId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfApplication(applicationId);
            }
        }
        return null;
    }

    public List<Message> consumeMessageOfApplication(final String centralName, final String applicationId, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfApplication(applicationId, amount);
            }
        }
        return new ArrayList<>();
    }
}
