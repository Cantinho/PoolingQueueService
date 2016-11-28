package br.com.example.controllers;

import br.com.example.bean.Message;
import br.com.example.exceptions.PoolingQueueException;
import br.com.example.sqs.SimpleMessageQueue;
import br.com.example.statistics.IRequestStatisticallyProfilable;
import br.com.example.statistics.IStatistics;
import br.com.example.statistics.PoolingQueueServiceStatistic;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jotajr on 26/09/16.
 */

@RestController
@ComponentScan("br.com.example")
public class Controller implements IRequestStatisticallyProfilable {

    private final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private List<IStatistics> poolingQueueServiceStatistics = new ArrayList<>();

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

        long startTimestamp = new Date().getTime();

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
                    if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                        tryingToCreateCentral(serialNumber);
                        try {
                            message = simpleMessageQueue.consumeMessageOfApplication(serialNumber, appID);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long endTimestamp = new Date().getTime();
                addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pa_pull", "Slave pulling only one message.");

                return new ResponseEntity<String>(gson.toJson(message), HttpStatus.OK);
            } else {
                LOGGER.info("messageAmount:"+messageAmount);
                List<Message> messages = null;
                try {
                    messages = simpleMessageQueue.consumeMessageOfApplication(serialNumber, appID, Integer.valueOf(messageAmount));
                } catch (PoolingQueueException e) {
                    //e.printStackTrace();
                    LOGGER.error("Unable to consume an application message from a nonexistent central [" + serialNumber + "].");
                    if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                        tryingToCreateCentral(serialNumber);
                        try {
                            messages = simpleMessageQueue.consumeMessageOfApplication(serialNumber, appID, Integer.valueOf(messageAmount));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long endTimestamp = new Date().getTime();
                addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pa_pull", "Slave pulling multiple messages.");

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
            if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                tryingToCreateCentral(serialNumber);
                try {
                    produced = simpleMessageQueue.produceMessageToCentral(serialNumber, message);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTimestamp = new Date().getTime();
        addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pa_post", "Slave posting.");

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

        long startTimestamp = new Date().getTime();

        /**
         * Message: SerialNumber, ApplicationID, Timestamp, Priority, Message
         */
        String timestamp = String.valueOf(new Date().getTime());
        String priority = "10";
        Message message = new Message(serialNumber, appID, timestamp, priority, packet);

        if(broadcast != null) {
            boolean broadcasted = false;
            try {
                broadcasted = simpleMessageQueue.broadcastMessageToApplication(serialNumber, appID, message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            long endTimestamp = new Date().getTime();
            addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pc_post", "Master posting in broadcast mode.");

            return new ResponseEntity<String>(broadcasted ? "OK" : "ERROR", HttpStatus.OK);
        } else {
            // post message to single application-id

            boolean produced = false;
            try {
                produced = simpleMessageQueue.produceMessageToApplication(serialNumber, appID, message);
                long endTimestamp = new Date().getTime();

                return new ResponseEntity<String>(produced ? "OK" : "ERROR", HttpStatus.OK);
            } catch (PoolingQueueException e) {
                //e.printStackTrace();
                LOGGER.error("Unable to produce an application message to a nonexistent central [" + serialNumber + "].");
                if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                    tryingToCreateCentral(serialNumber);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            long endTimestamp = new Date().getTime();
            addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pc_post", "Master posting for only one Slave.");

            return new ResponseEntity<String>(produced ? "OK" : "ERROR", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/pull", method = RequestMethod.GET)
    public ResponseEntity<String> pull(@RequestHeader(value = "Serial-Number") String serialNumber) {

        long startTimestamp = new Date().getTime();

        /** espera-se que o servico em nuvem leia a pilha do sqs e retorne uma resposta para aquela central
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
            if(e.getCode() == PoolingQueueException.CENTRAL_NOT_FOUND) {
                tryingToCreateCentral(serialNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTimestamp = new Date().getTime();
        addPollingQueueServiceStatistic(startTimestamp, endTimestamp, "pc_pull", "Master pulling.");

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

    private void addPollingQueueServiceStatistic(long startTimestamp, long endTimestamp, String label, String message){
        long totalTime = endTimestamp - startTimestamp;
        PoolingQueueServiceStatistic poolingQueueServiceStatistic = new PoolingQueueServiceStatistic(label, totalTime, message);
        poolingQueueServiceStatistics.add(poolingQueueServiceStatistic);
    }


    @Override
    public List<IStatistics> collectStatistics() {
        return poolingQueueServiceStatistics;
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public ResponseEntity<String> statistics() {
        System.out.println(collectStatistics());
        return new ResponseEntity<String>("OK", HttpStatus.OK);
    }
}
