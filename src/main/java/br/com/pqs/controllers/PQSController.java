package br.com.pqs.controllers;

import br.com.pqs.sqs.model.MessageMapper;
import br.com.pqs.sqs.service.PoolingQueueService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jordaoesa on 05/12/16.
 */
@RestController
@ComponentScan("br.com.jfl")
public class PQSController {

    @Autowired
    private PoolingQueueService poolingQueueService;

    @RequestMapping(value = "/cconn", method = RequestMethod.POST)
    public ResponseEntity<String> cconn(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.cconn(serialNumber, contentType, message);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }

    @RequestMapping(value = "/cpull", method = RequestMethod.GET)
    public ResponseEntity<String> cpull(@RequestHeader(value = "Serial-Number") String serialNumber) {

        MessageMapper responseMessage = poolingQueueService.cpull(serialNumber);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }

    @RequestMapping(value = "/cpush", method = RequestMethod.POST)
    public ResponseEntity<String> cpush(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.cpush(serialNumber, applicationID, broadcast, contentType, message);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }

    @RequestMapping(value = "/aconn", method = RequestMethod.POST)
    public ResponseEntity<String> aconn(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.aconn(serialNumber, applicationID, contentType, message);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }

    @RequestMapping(value = "/apull", method = RequestMethod.GET)
    public ResponseEntity<String> apull(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Message-Amount", required = false) String messageAmount) {

        MessageMapper responseMessage = poolingQueueService.apull(serialNumber, applicationID, messageAmount);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }



    @RequestMapping(value = "/apush", method = RequestMethod.POST)
    public ResponseEntity<String> apush(@RequestHeader(value = "Serial-Number") String serialNumber,
                                        @RequestHeader(value = "Application-ID") String applicationID,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = poolingQueueService.apush(serialNumber, applicationID, contentType, message);

        return new ResponseEntity<String>(new Gson().toJson(responseMessage), HttpStatus.OK);
    }

}
