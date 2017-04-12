package com.cantinho.cms.bean;

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
public class Message {

    private String senderId;
    private String receiverId;
    private String timestamp;
    private String priority;
    private String message;

    public Message() {}

    public Message(String senderId, String receiverId, String timestamp, String priority, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.priority = priority;
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static String parseToMinimalistString(final Message message) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(message.getSenderId() + ";");
        strBuilder.append(message.getReceiverId() + ";");
        strBuilder.append(message.getTimestamp() + ";");
        strBuilder.append(message.getPriority() + ";");
        strBuilder.append(message.getMessage());

        return strBuilder.toString();
    }

    public static Message parseMinimalistStringToMessage(final String message) throws Exception {
        String[] parts = message.split(";");
        if(parts.length == 5) {
            return new Message(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }
        throw new Exception("Fail to parse minimalist string to message");
    }

}
