package br.com.example.controllers;

import br.com.example.bean.Message;
import br.com.example.sqs.SimpleMessageQueue;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by jotajr on 26/09/16.
 */

@RestController
@ComponentScan("br.com.example")
public class Controller {

    private final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private SimpleMessageQueue simpleMessageQueue;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> index() {
        return new ResponseEntity<String>("OK", HttpStatus.OK);
    }

    /**
     * if packet == NULL:
     * este comando deve ser tratado como pulling
     * <p>
     * responseHeader:
     * <p>
     * responseBody:
     * if header Message-Amount do request não existe, recebe-se até 1 mensagem.
     * messages:[
     * {P:""}
     * ] or
     * if existe header Message-Amount, recebe-se até N mensagens.
     * messages:[
     * {P:"mensagem hexadecimal"},
     * {P:"mensagem hexadecimal"},
     * {P:"mensagem hexadecimal"}
     * }
     * if {P:""}: nada na pilha para esta aplicacao
     * if {P:"mensagem hexadecimal"}: comando da central;
     */
    @RequestMapping(value = "/pa", method = RequestMethod.POST)
    public ResponseEntity<String> pa(@RequestHeader(value = "Serial-Number") String serialNumber,
                                     @RequestHeader(value = "Application-ID") String appID,
                                     @RequestHeader(value = "Message-Amount", required = false) String messageAmount,
                                     @RequestBody(required = false) String packet) {

        Gson gson = new Gson();

        if (packet == null) {
            LOGGER.info("packet nulo");
            if(messageAmount == null) {
                LOGGER.info("messageAmount nulo");
                Message message = simpleMessageQueue.consumeMessageOfApplication();
                return new ResponseEntity<String>(gson.toJson(message), HttpStatus.OK);
            } else {
                LOGGER.info("messageAmount:"+messageAmount);
                List<Message> messages = simpleMessageQueue.consumeMessageOfApplication(Integer.valueOf(messageAmount));
                return new ResponseEntity<String>(gson.toJson(messages), HttpStatus.OK);
            }
        }

        /**
         * Message: SerialNumber, ApplicationID, Timestamp, Priority, Message
         */
        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, appID, timestamp, priority, packet);
        boolean produced = simpleMessageQueue.produceMessageToCentral(message);

        return new ResponseEntity<String>(produced ? "OK" : "ERROR", HttpStatus.OK);


    }

    @RequestMapping(value = "/pc", method = RequestMethod.POST)
    public ResponseEntity<String> pc(@RequestHeader(value = "Serial-Number") String serialNumber,
                                     @RequestHeader(value = "Application-ID") String appID,
                                     @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                     @RequestHeader(value = "Multicast", required = false) String multicast,
                                     @RequestBody String request) {

        
        return new ResponseEntity<String>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/pull", method = RequestMethod.GET)
    public ResponseEntity<String> pull(@RequestHeader(value = "Serial-Number") String serialNumber) {

        /* espera-se que o servico em nuvem leia a pilha do sqs e retorne uma resposta para aquela central
         *
         * responseHeaders:
         *      "Serial-Number"
         *      "Application-ID"
         * responseBody:
         *      "packet"
         */

        return new ResponseEntity<String>("", HttpStatus.OK);
    }


}
