package br.com.pqs.sqs.service;


import br.com.pqs.bean.PQSResponse;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.processor.IMessageProcessor;
import br.com.processor.mapper.MessageMapper;

/**
 * Created by jordaoesa on 05/12/16.
 */
public interface PoolingQueueService {

    void setIMessageProcessor(final IMessageProcessor iMessageProcessor) throws PoolingQueueException;

    MessageMapper cconn(String serialNumber, String contentType, MessageMapper messageMapper);

    PQSResponse cpull(String serialNumber);

    MessageMapper cpush(String serialNumber, String applicationID, String broadcast, String contentType, MessageMapper messageMapper);

    MessageMapper aconn(String serialNumber, String applicationID, String contentType, MessageMapper messageMapper);

    PQSResponse apull(String serialNumber, String applicationID, String messageAmount);

    MessageMapper apush(String serialNumber, String applicationID, String contentType, MessageMapper messageMapper);

    boolean isconn(String serialNumber);
}
