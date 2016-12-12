package br.com.pqs.sqs.service;


import br.com.pqs.sqs.model.MessageMapper;

/**
 * Created by jordaoesa on 05/12/16.
 */
public interface PoolingQueueService {

    MessageMapper cconn(String serialNumber, String contentType, MessageMapper messageMapper);

    MessageMapper cpull(String serialNumber);

    MessageMapper cpush(String serialNumber, String applicationID, String broadcast, String contentType, MessageMapper messageMapper);

    MessageMapper aconn(String serialNumber, String applicationID, String contentType, MessageMapper messageMapper);

    MessageMapper apull(String serialNumber, String applicationID, String messageAmount);

    MessageMapper apush(String serialNumber, String applicationID, String contentType, MessageMapper messageMapper);

}
