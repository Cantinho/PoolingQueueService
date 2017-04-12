package com.cantinho.cms.exceptions;

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
public class CloudiaMessageException extends Exception {

    public static final int MASTER_NOT_FOUND = 400;
    public static final int SLAVE_NOT_FOUND = 500;
    public static final int INVALID_CONTENT_TYPE = 600;

    private int code;

    public CloudiaMessageException(String message) {
        super(message);
        this.code = -1;
    }

    public CloudiaMessageException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(CloudiaMessageException.class.getName() + ":[");
        strBuilder.append("message:" + getMessage() + ";");
        strBuilder.append("code:" + getCode());
        strBuilder.append("]");
        return strBuilder.toString();
    }
}
