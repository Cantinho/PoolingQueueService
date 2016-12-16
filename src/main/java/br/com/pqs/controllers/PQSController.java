package br.com.pqs.controllers;

import br.com.pqs.bean.PQSResponse;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.service.PoolingQueueService;
import br.com.processor.CloudiaMessage;
import br.com.processor.CloudiaMessageProcessor;
import br.com.processor.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

/**
 * Created by jordaoesa on 05/12/16.
 */
@RestController
@ComponentScan("br.com.jfl")
public class PQSController {

    @Autowired
    private PoolingQueueService poolingQueueService;

    @PostConstruct
    void init() {
        System.out.println("INIT - POST CONSTRUCT");
        try {
            poolingQueueService.setIMessageProcessor(new CloudiaMessageProcessor());
        } catch (PoolingQueueException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/cconn", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> cconn(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.cconn(serialNumber, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/cpull", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> cpull(@RequestHeader(value = "Serial-Number") String serialNumber) {

        PQSResponse pqsResponse = poolingQueueService.cpull(serialNumber);

        return new ResponseEntity<MessageMapper>(pqsResponse.getBody(), pqsResponse.getHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/cpush", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> cpush(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID", required = false) String applicationID,
                                        @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.cpush(serialNumber, applicationID, broadcast, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/aconn", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> aconn(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.aconn(serialNumber, applicationID, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/apull", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> apull(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Message-Amount", required = false) String messageAmount) {

        PQSResponse pqsResponse = poolingQueueService.apull(serialNumber, applicationID, messageAmount);

        return new ResponseEntity<MessageMapper>(pqsResponse.getBody(), pqsResponse.getHeaders(), HttpStatus.OK);
    }



    @RequestMapping(value = "/apush", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> apush(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.apush(serialNumber, applicationID, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/isconn", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> isconn(@RequestHeader(value = "Serial-Number") String serialNumber) {

        boolean isconn = poolingQueueService.isconn(serialNumber);
        MessageMapper responseMessage = new MessageMapper();
        responseMessage.setMsg(isconn ? CloudiaMessage.OK : CloudiaMessage.ERROR);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

}
