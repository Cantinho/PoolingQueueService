package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.SimpleMessageQueue;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static br.com.pqs.exceptions.PoolingQueueException.CENTRAL_NOT_FOUND;

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

    public void createPoolingQueue(final String queueName) throws Exception {
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
              poolingQueueMap.put(queueName, amazonPoolingQueue);
          }
        }

        LOGGER.info("SimpleMessageQueueImpl instance creating a PoolingQueue [" + queueName + "].");
        PoolingQueue poolingQueue = new PoolingQueue(queueName);
        poolingQueueMap.put(queueName, poolingQueue);
    }

    public boolean produceMessageToCentral(final String centralName, final Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).produceMessageToCentral(message);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public Message peekMessageOfCentral(final String centralName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).peekMessageOfCentral();
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public Message consumeMessageOfCentral(final String centralName) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfCentral();
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public List<Message> consumeMessageOfCentral(final String centralName, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfCentral(amount);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }



    public boolean produceMessageToApplication(final String centralName, final String applicationId, Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).produceMessageToApplication(applicationId, message);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    @Override
    public boolean broadcastMessageToApplication(String centralName, String applicationId, Message message) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).broadcastMessageToApplication(applicationId, message);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public Message peekMessageOfApplication(final String centralName, final String applicationId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).peekMessageOfApplication(applicationId);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public Message consumeMessageOfApplication(final String centralName, final String applicationId) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfApplication(applicationId);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

    public List<Message> consumeMessageOfApplication(final String centralName, final String applicationId, final int amount) throws Exception {
        synchronized (poolingQueueLock) {
            if(poolingQueueMap.containsKey(centralName)) {
                return poolingQueueMap.get(centralName).consumeMessageOfApplication(applicationId, amount);
            }
        }
        throw new PoolingQueueException("Central [" + centralName + "] does not exist.", CENTRAL_NOT_FOUND);
    }

}
