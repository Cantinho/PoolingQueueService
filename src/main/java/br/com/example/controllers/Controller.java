package br.com.example.controllers;

import br.com.example.bean.Message;
import br.com.example.exceptions.PoolingQueueException;
import br.com.example.sqs.SimpleMessageQueue;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        // This method runs after the controller has been created.
        // Uncomment the following line to customize a pooling queue implementation.
        // simpleMessageQueue.setPoolingQueueClassName(SOME_POOLING_QUEUE_IMPLEMENTATION_NAME_HERE);
    }

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
                Message message = null;
                try {
                    message = simpleMessageQueue.consumeMessageOfApplication(serialNumber, appID);
                } catch (PoolingQueueException e) {
                    //e.printStackTrace();
                    LOGGER.error("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
                    tryingToCreateCentral(serialNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ResponseEntity<String>(gson.toJson(message), HttpStatus.OK);
            } else {
                LOGGER.info("messageAmount:"+messageAmount);
                List<Message> messages = null;
                try {
                    messages = simpleMessageQueue.consumeMessageOfApplication(serialNumber, appID, Integer.valueOf(messageAmount));
                } catch (PoolingQueueException e) {
                    //e.printStackTrace();
                    LOGGER.error("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
                    tryingToCreateCentral(serialNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ResponseEntity<String>(gson.toJson(messages), HttpStatus.OK);
            }
        }

        /**
         * Message: SerialNumber, ApplicationID, Timestamp, Priority, Message
         */
        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, appID, timestamp, priority, packet);
        boolean produced = false;
        try {
            produced = simpleMessageQueue.produceMessageToCentral(serialNumber, message);
        } catch (PoolingQueueException e) {
            //e.printStackTrace();
            LOGGER.error("Unable to produce an application message from a nonexistent central [" + serialNumber + "].");
            tryingToCreateCentral(serialNumber);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Trying to create a central queue before resend the message.");
            try {
                simpleMessageQueue.createPoolingQueue(serialNumber);
                produced = simpleMessageQueue.produceMessageToCentral(serialNumber, message);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return new ResponseEntity<String>(produced ? "OK" : "ERROR", HttpStatus.OK);


    }

    /**
     * Post message to application or applications (broadcast) of the same serialNumber (central).
     * Header has a size limit. Maybe Multicast header have to be disabled.
     *
     * @param serialNumber
     * @param appID
     * @param broadcast
     * @param multicast
     * @param packet
     * @return
     */
    @RequestMapping(value = "/pc", method = RequestMethod.POST)
    public ResponseEntity<String> pc(@RequestHeader(value = "Serial-Number") String serialNumber,
                                     @RequestHeader(value = "Application-ID") String appID,
                                     @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                     @RequestHeader(value = "Multicast", required = false) String multicast,
                                     @RequestBody String packet) {

        /**
         * Message: SerialNumber, ApplicationID, Timestamp, Priority, Message
         */
        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, appID, timestamp, priority, packet);

        if(broadcast != null) {
            boolean broadcasted = simpleMessageQueue.broadcastMessageToApplication(serialNumber, appID, message);
        } else {
            // post message to single application-id

            boolean produced = false;
            try {
                produced = simpleMessageQueue.produceMessageToApplication(serialNumber, appID, message);
                return new ResponseEntity<String>(produced ? "OK" : "ERROR", HttpStatus.OK);
            } catch (PoolingQueueException e) {
                //e.printStackTrace();
                LOGGER.error("Unable to produce an application message to a nonexistent central [" + serialNumber + "].");
                tryingToCreateCentral(serialNumber);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("Trying to create a central queue before resend the message.");
                try {
                    simpleMessageQueue.createPoolingQueue(serialNumber);
                    produced = simpleMessageQueue.produceMessageToApplication(serialNumber, appID, message);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        
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

        Message message = null;
        try {
            message = simpleMessageQueue.consumeMessageOfCentral(serialNumber);
        }  catch (PoolingQueueException e) {
            //e.printStackTrace();
            LOGGER.error("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
            tryingToCreateCentral(serialNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>(message == null ? "{}" : new Gson().toJson(message), HttpStatus.OK);
    }


    private void tryingToCreateCentral(final String serialNumber) {
            LOGGER.error("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
            try {
                LOGGER.info("Trying to create a central [" + serialNumber + "].");
                simpleMessageQueue.createPoolingQueue(serialNumber);
            } catch (Exception e1) {
                LOGGER.error("It failed miserably in creating a new central [" + serialNumber + "].");
                e1.printStackTrace();
            }
    }

}
