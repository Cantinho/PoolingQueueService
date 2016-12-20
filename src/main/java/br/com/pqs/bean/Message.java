package br.com.pqs.bean;

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
    private String masterSN;
    private String slaveID;
    private String Timestamp;
    private String Priority;
    private String Message;

    public Message() {}

    public Message(String masterSN, String slaveID, String timestamp, String priority, String message) {
        this.masterSN = masterSN;
        this.slaveID = slaveID;
        Timestamp = timestamp;
        Priority = priority;
        Message = message;
    }

    public String getMasterSN() {
        return masterSN;
    }

    public void setMasterSN(String masterSN) {
        this.masterSN = masterSN;
    }

    public String getSlaveID() {
        return slaveID;
    }

    public void setSlaveID(String slaveID) {
        this.slaveID = slaveID;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public static String parseToMinimalistString(final Message message) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(message.getMasterSN() + ";");
        strBuilder.append(message.getSlaveID() + ";");
        strBuilder.append(message.getTimestamp() + ";");
        strBuilder.append(message.getPriority() + ";");
        strBuilder.append(message.getMessage());

        return strBuilder.toString();
    }

    public static Message parseMinimalistStringToMessage(final String message) throws Exception {
        String[] parts = message.split(";");
        if(parts.length == 5) {
            return new br.com.pqs.bean.Message(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }
        throw new Exception("Fail to parse minimalist string to Message");
    }

}
