package br.com.pqs.sqs.impl;

import br.com.pqs.bean.Message;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jordaoesa on 07/12/16.
 */
public class AmazonSQSDriver implements AmazonSQSApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSQSDriver.class);
    /*
     * The ProfileCredentialsProvider returns your [default]
     * credential profile by reading from the credentials file located at
     * (~/.aws/credentials).
     */
    private AWSCredentials credentials = null;
    private AmazonSQSClient simpleQueueService = null;
    private final String baseUrl;
    private final String accountId;
    private final String queueType;

    //String myQueueUrl = "https://sqs.us-east-2.amazonaws.com/796804300465/MyFifoQueue.fifo";

    public AmazonSQSDriver(final String baseUrl, final String accountId, final String  queueType) throws br.com.pqs.exceptions.AmazonSQSException {
        if(baseUrl == null) {
            this.baseUrl = "https://sqs.us-east-2.amazonaws.com";
        } else {
            this.baseUrl = baseUrl; // default "https://sqs.us-east-2.amazonaws.com";
        }
        LOGGER.info("Endpoint ... coloca aqui: " + this.baseUrl);

        if(accountId == null){
            throw new br.com.pqs.exceptions.AmazonSQSException("You must inform a valid accountId.", br.com.pqs.exceptions.AmazonSQSException.INVALID_CREDENTIALS);
        }
        this.accountId = accountId; // "796804300465"
        LOGGER.info("AccountId ... coloca aqui: " + this.accountId);

        if(queueType == null){
            this.queueType = "fifo"; // default fifo
        } else {
            this.queueType = queueType;
        }
        LOGGER.info("QueueType ... coloca aqui: " + this.queueType);

        // creating credentials
        createCredentials(baseUrl, accountId, queueType);
    }

    private void createCredentials(final String baseUrl, final String accountId, final String  queueType) {
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Can't load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is a in valid format.",
                    e);
        }

        simpleQueueService = new AmazonSQSClient(credentials);
        simpleQueueService.setEndpoint(baseUrl);
        LOGGER.info("=======================================================");
        LOGGER.info("Getting Started with Amazon SQS FIFO Queues");
        LOGGER.info("=======================================================\n");
    }

    private final String getQueueUrl(final String queueName) {
        return baseUrl + "/" + accountId + "/" + queueName + "." + queueType;
    }

    @Override
    public boolean sendMessage(String queueName, Message message) {
        if(credentials != null && simpleQueueService != null) {
            // Send a message
            LOGGER.info("Sending a message to " + queueName + ".\n");
            SendMessageRequest sendMessageRequest = new SendMessageRequest(getQueueUrl(queueName), message.getMessage());
            // You must provide a non-empty MessageGroupId when sending messages to a FIFO queue
            sendMessageRequest.setMessageGroupId("messageGroup1");
            // Uncomment the following to provide the MessageDeduplicationId
            // sendMessageRequest.setMessageDeduplicationId("1");
            SendMessageResult sendMessageResult = simpleQueueService.sendMessage(sendMessageRequest);
            String sequenceNumber = sendMessageResult.getSequenceNumber();
            String messageId = sendMessageResult.getMessageId();
            LOGGER.info("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
        }
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
