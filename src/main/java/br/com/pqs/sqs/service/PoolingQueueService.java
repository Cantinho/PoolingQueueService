package br.com.pqs.sqs.service;


import br.com.pqs.bean.PQSResponse;
import br.com.pqs.exceptions.PoolingQueueException;
import br.com.processor.IMessageProcessor;
import br.com.processor.mapper.SimpleMessageMapper;

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
public interface PoolingQueueService {

    void setIMessageProcessor(final IMessageProcessor iMessageProcessor) throws PoolingQueueException;

    SimpleMessageMapper mconn(String serialNumber, String contentType, SimpleMessageMapper messageMapper);

    PQSResponse mpull(String serialNumber);

    SimpleMessageMapper mpush(String serialNumber, String slaveId, String broadcast, String contentType, SimpleMessageMapper messageMapper);

    SimpleMessageMapper sconn(String serialNumber, String slaveId, String contentType, SimpleMessageMapper messageMapper);

    PQSResponse spull(String serialNumber, String slaveId, String messageAmount);

    SimpleMessageMapper spush(String serialNumber, String slaveId, String contentType, SimpleMessageMapper messageMapper);

    boolean isconn(String serialNumber);
}
