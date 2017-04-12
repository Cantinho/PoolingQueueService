package com.cantinho.cms.controllers;

import com.cantinho.cms.sqs.MessageQueue;
import com.cantinho.cms.statistics.IRequestStatisticallyProfilable;
import com.cantinho.cms.statistics.IStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

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
public class Controller implements IRequestStatisticallyProfilable {

    private final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private List<IStatistics> cloudiaMessageServiceStatistics = new ArrayList<>();

    @Autowired
    private MessageQueue messageQueue;

    @PostConstruct
    public void init() {
        // This method runs after the controller has been created.
        // Uncomment the following line to customize a message queue implementation.
        // messageQueue.setMessageQueueClassName(SOME_MESSAGE_QUEUE_IMPLEMENTATION_NAME_HERE);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> index() {
        return new ResponseEntity<String>("OK", HttpStatus.OK);
    }


    @Override
    public List<IStatistics> collectStatistics() {
        return cloudiaMessageServiceStatistics;
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public ResponseEntity<String> statistics() {
        List<IStatistics> statistics = collectStatistics();
        StringBuilder builder = new StringBuilder();
        for(IStatistics statistic : statistics){
            builder.append(statistic.toString() + "\n");
        }
        System.out.println(builder.toString());
        return new ResponseEntity<String>(builder.toString(), HttpStatus.OK);
    }
}
