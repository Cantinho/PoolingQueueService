package br.com.pqs.sqs.impl;

import com.amazonaws.services.sqs.model.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
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
        LOGGER.info("Endpoint (baseurl): " + this.baseUrl);

        if(accountId == null){
            throw new br.com.pqs.exceptions.AmazonSQSException("You must inform a valid accountId.", br.com.pqs.exceptions.AmazonSQSException.INVALID_CREDENTIALS);
        }
        this.accountId = accountId; // "796804300465"
        LOGGER.info("Account ID: " + this.accountId);

        if(queueType == null){
            this.queueType = "fifo"; // default fifo
        } else {
            this.queueType = queueType;
        }
        LOGGER.info("Queue type: " + this.queueType);

        // creating credentials
        createCredentials(baseUrl);
    }

    private void createCredentials(final String baseUrl) {
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
    public SendMessageResult sendMessage(String queueName, String message) {
        if(credentials != null && simpleQueueService != null) {
            try {
                // Send a message
                LOGGER.info("Sending a message to " + queueName + ".\n");
                SendMessageRequest sendMessageRequest = new SendMessageRequest(getQueueUrl(queueName), message);
                // You must provide a non-empty MessageGroupId when sending messages to a FIFO queue
                sendMessageRequest.setMessageGroupId("1");
                // Uncomment the following to provide the MessageDeduplicationId
                // sendMessageRequest.setMessageDeduplicationId("1");
                SendMessageResult sendMessageResult = simpleQueueService.sendMessage(sendMessageRequest);
                String sequenceNumber = sendMessageResult.getSequenceNumber();
                String messageId = sendMessageResult.getMessageId();
                LOGGER.info("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
                return sendMessageResult;
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean sendMessage(String queueName, List<String> messages) {
        return false;
    }

    @Override
    public List<Message> receiveMessage(String queueName, int messageAmount) {
        if(credentials != null && simpleQueueService != null) {
            try {
                //LOGGER.info("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
                final String queueUrl = getQueueUrl(queueName);
                System.out.println("Receiving messages from " + queueUrl + ".\n");
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
                // Uncomment the following to provide the ReceiveRequestDeduplicationId
                receiveMessageRequest.setReceiveRequestAttemptId("1");
                receiveMessageRequest.setMaxNumberOfMessages(messageAmount);
                return simpleQueueService.receiveMessage(receiveMessageRequest).getMessages();
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public List<Message> receiveMessage(ReceiveMessageRequest receiveMessageRequest) {
        if(credentials != null && simpleQueueService != null) {
            try {
                // Uncomment the following to provide the ReceiveRequestDeduplicationId
                System.out.println("Receiving messages from " + receiveMessageRequest.getQueueUrl() + ".\n");
                return simpleQueueService.receiveMessage(receiveMessageRequest).getMessages();
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public DeleteMessageResult deleteMessage(final String queueName, final String messageReceiptHandle) {
        if(credentials != null && simpleQueueService != null) {
            //LOGGER.info("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
            final String queueUrl = getQueueUrl(queueName);
            return simpleQueueService.deleteMessage(queueUrl, messageReceiptHandle);
        }
        return null;
    }

    @Override
    public DeleteMessageResult deleteMessage(DeleteMessageRequest deleteMessageRequest) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.deleteMessage(deleteMessageRequest);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public ChangeMessageVisibilityResult changeMessageVisibility(String queueName, String receiptHandle, final Integer visibilityTimeout) {
        if(credentials != null && simpleQueueService != null) {
            return simpleQueueService.changeMessageVisibility(getQueueUrl(queueName), receiptHandle, visibilityTimeout);
        }
        return null;
    }

    @Override
    public CreateQueueResult createQueue(String queueName) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.createQueue(queueName);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public CreateQueueResult createQueue(CreateQueueRequest createQueueRequest) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.createQueue(createQueueRequest);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public ListQueuesResult listQueues() {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.listQueues();
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public DeleteQueueResult deleteQueue(String queueName) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.deleteQueue(getQueueUrl(queueName));
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public DeleteQueueResult deleteQueue(DeleteQueueRequest deleteQueueRequest) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.deleteQueue(deleteQueueRequest);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public PurgeQueueResult purgeQueue(PurgeQueueRequest purgeQueueRequest) {
        if(credentials != null && simpleQueueService != null) {
            try {
                return simpleQueueService.purgeQueue(purgeQueueRequest);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                return null;
            }
        }
        return null;
    }
}
