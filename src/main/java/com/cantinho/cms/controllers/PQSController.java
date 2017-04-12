package com.cantinho.cms.controllers;

import br.com.processor.mapper.MessageMapper;
import com.cantinho.cms.bean.CMSResponse;
import com.cantinho.cms.exceptions.CloudiaMessageException;
import com.cantinho.cms.sqs.service.CloudiaMessageService;
import br.com.processor.SimpleMessage;
import br.com.processor.SimpleMessageProcessor;
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
@ComponentScan("com.cantinho.cms")
public class PQSController {

    @Autowired
    private CloudiaMessageService cloudiaMessageService;

    @PostConstruct
    void init() {
        System.out.println("INIT - POST CONSTRUCT");
        try {
            cloudiaMessageService.setIMessageProcessor(new SimpleMessageProcessor());
        } catch (CloudiaMessageException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/mconn", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> mconn(@RequestHeader(value = "Master-ID") String masterSN,
                                               @RequestHeader(value = "Content-Type") String contentType,
                                               @RequestBody MessageMapper message) {

        MessageMapper responseMessage = cloudiaMessageService.mconn(masterSN, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/mpull", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> mpull(@RequestHeader(value = "Master-ID") String masterSN) {

        CMSResponse CMSResponse = cloudiaMessageService.mpull(masterSN);

        return new ResponseEntity<MessageMapper>(CMSResponse.getBody(), CMSResponse.getHeaders(), HttpStatus.OK);
    }

    @RequestMapping(value = "/mpush", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> mpush(@RequestHeader(value = "Master-ID") String masterSN,
                                        @RequestHeader(value = "Slave-ID", required = false) String slaveId,
                                        @RequestHeader(value = "Broadcast", required = false) String broadcast,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = cloudiaMessageService.mpush(masterSN, slaveId, broadcast, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/sconn", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> sconn(@RequestHeader(value = "Master-ID") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = cloudiaMessageService.sconn(masterSN, slaveId, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/spull", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> spull(@RequestHeader(value = "Master-ID") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Message-Amount", required = false) String messageAmount) {

        CMSResponse CMSResponse = cloudiaMessageService.spull(masterSN, slaveId, messageAmount);

        return new ResponseEntity<MessageMapper>(CMSResponse.getBody(), CMSResponse.getHeaders(), HttpStatus.OK);
    }



    @RequestMapping(value = "/spush", method = RequestMethod.POST)
    public ResponseEntity<MessageMapper> spush(@RequestHeader(value = "Master-ID") String masterSN,
                                        @RequestHeader(value = "Slave-ID") String slaveId,
                                        @RequestHeader(value = "Content-Type") String contentType,
                                        @RequestBody MessageMapper message) {

        MessageMapper responseMessage = cloudiaMessageService.spush(masterSN, slaveId, contentType, message);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/isconn", method = RequestMethod.GET)
    public ResponseEntity<MessageMapper> isconn(@RequestHeader(value = "Master-ID") String masterSN) {

        boolean isconn = cloudiaMessageService.misconn(masterSN);
        MessageMapper responseMessage = new MessageMapper();
        responseMessage.setMessage(isconn ? SimpleMessage.OK : SimpleMessage.ERROR);

        return new ResponseEntity<MessageMapper>(responseMessage, HttpStatus.OK);
    }

}
