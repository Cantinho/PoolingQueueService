package br.com.pqs.controllers;

import br.com.pqs.bean.PQSResponse;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.pqs.sqs.service.PoolingQueueService;
import br.com.processor.SimpleMessage;
import br.com.processor.SimpleMessageProcessor;
import br.com.processor.mapper.SimpleMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

/**
 * Copyright 2016 Cantinho. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * @author Samir Trajano Feitosa
 * @author Jordão Ezequiel Serafim de Araújo
 * @author Cantinho - Github https://github.com/Cantinho
 * @since 2016
 * @license Apache 2.0
 *
 * This file is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 */
@RestController
@ComponentScan("br.com.pqs")
public class PQSController {

    @Autowired
    private PoolingQueueService poolingQueueService;

    @PostConstruct
    void init() {
        System.out.println("INIT - POST CONSTRUCT");
        try {
            poolingQueueService.setIMessageProcessor(new SimpleMessageProcessor());
        } catch (PoolingQueueException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/mconn", method = RequestMethod.POST)
    public ResponseEntity<SimpleMessageMapper> mconn(@RequestHeader(value = "Master-SN") String masterSN,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody SimpleMessageMapper message) {

        SimpleMessageMapper responseMessage = poolingQueueService.mconn(masterSN, contentType, message);

        return new ResponseEntity<SimpleMessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/mpull", method = RequestMethod.GET)
    public ResponseEntity<SimpleMessageMapper> mpull(@RequestHeader(value = "Master-SN") String masterSN) {

        PQSResponse pqsResponse = poolingQueueService.mpull(masterSN);

        return new ResponseEntity<SimpleMessageMapper>(pqsResponse.getBody(), pqsResponse.getHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/mpush", method = RequestMethod.POST)
    public ResponseEntity<SimpleMessageMapper> mpush(@RequestHeader(value = "Master-SN") String masterSN,
                                        @RequestHeader(value = "Slave-ID", required = false) String slaveId,
                                        @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody SimpleMessageMapper message) {

        SimpleMessageMapper responseMessage = poolingQueueService.mpush(masterSN, slaveId, broadcast, contentType, message);

        return new ResponseEntity<SimpleMessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/sconn", method = RequestMethod.POST)
    public ResponseEntity<SimpleMessageMapper> sconn(@RequestHeader(value = "Master-SN") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody SimpleMessageMapper message) {

        SimpleMessageMapper responseMessage = poolingQueueService.sconn(masterSN, slaveId, contentType, message);

        return new ResponseEntity<SimpleMessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/spull", method = RequestMethod.GET)
    public ResponseEntity<SimpleMessageMapper> spull(@RequestHeader(value = "Master-SN") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Message-Amount", required = false) String messageAmount) {

        PQSResponse pqsResponse = poolingQueueService.spull(masterSN, slaveId, messageAmount);

        return new ResponseEntity<SimpleMessageMapper>(pqsResponse.getBody(), pqsResponse.getHeaders(), HttpStatus.OK);
    }



    @RequestMapping(value = "/spush", method = RequestMethod.POST)
    public ResponseEntity<SimpleMessageMapper> spush(@RequestHeader(value = "Master-SN") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody SimpleMessageMapper message) {

        SimpleMessageMapper responseMessage = poolingQueueService.spush(masterSN, slaveId, contentType, message);

        return new ResponseEntity<SimpleMessageMapper>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/isconn", method = RequestMethod.GET)
    public ResponseEntity<SimpleMessageMapper> isconn(@RequestHeader(value = "Master-SN") String masterSN) {

        boolean isconn = poolingQueueService.isconn(masterSN);
        SimpleMessageMapper responseMessage = new SimpleMessageMapper();
        responseMessage.setMessage(isconn ? SimpleMessage.OK : SimpleMessage.ERROR);

        return new ResponseEntity<SimpleMessageMapper>(responseMessage, HttpStatus.OK);
    }

}
