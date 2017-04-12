package com.cantinho.cms.sqs.service;

import br.com.processor.mapper.MessageMapper;
import com.cantinho.cms.bean.CMSResponse;
import com.cantinho.cms.exceptions.CloudiaMessageException;
import br.com.processor.IMessageProcessor;

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
public interface CloudiaMessageService {

    void setIMessageProcessor(final IMessageProcessor iMessageProcessor) throws CloudiaMessageException;

    boolean misconn(String masterId);

    MessageMapper mconn(String masterId, String contentType, MessageMapper messageMapper);

    CMSResponse mpull(String serialNumber);

    MessageMapper mpush(String masterId, String slaveId, String broadcast, String contentType, MessageMapper messageMapper);

    boolean sisconn(String slaveId);

    MessageMapper sconn(String masterId, String slaveId, String contentType, MessageMapper messageMapper);

    CMSResponse spull(String masterId, String slaveId, String messageAmount);

    MessageMapper spush(String masterId, String slaveId, String contentType, MessageMapper messageMapper);

}
