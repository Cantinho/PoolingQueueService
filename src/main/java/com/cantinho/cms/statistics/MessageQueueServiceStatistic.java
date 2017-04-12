package com.cantinho.cms.statistics;

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
public class MessageQueueServiceStatistic implements IStatistics {

    private static long globalSequence = 1;

    private long sequence;
    private String label;
    private long startTime;
    private long endTime;
    private String message;

    public MessageQueueServiceStatistic(String label, long startTime, long endTime, String message) {
        synchronized (MessageQueueServiceStatistic.class) {
            this.sequence = globalSequence++;
        }
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
    }

    public MessageQueueServiceStatistic(String label, long startTime, long endTime) {
        this(label, startTime, endTime, "");
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public long getTotalTime() {
        return endTime-startTime;
    }

    @Override
    public String print(boolean messageSuppressed) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getName() + ":[");
        stringBuilder.append("sequence:" + sequence + ";");
        stringBuilder.append("label:" + label + ";");
        stringBuilder.append("totalTime:" + (endTime-startTime) + ";");
        if(!messageSuppressed) stringBuilder.append("message:" + message);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     *  Sequence; StartTime; EndTime; TotalTime; Label;  Message
     *  00005;    normal;    normal;  000005;    normal; normal
     */
    public String csv(boolean messageSuppressed){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%06d;", sequence));
        builder.append(String.format("%d;", startTime));
        builder.append(String.format("%d;", endTime));
        builder.append(String.format("%06d;", (endTime-startTime)));
        builder.append(label + ";");
        builder.append(message);

        return builder.toString();
    }

    @Override
    public String toString() {
        return csv(false);
    }
}
