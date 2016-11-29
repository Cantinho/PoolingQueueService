package br.com.example.statistics;

import com.sun.javafx.binding.StringFormatter;

/**
 * Created by jordaoesa on 28/11/16.
 */
public class PoolingQueueServiceStatistic implements IStatistics {

    private static long globalSequence = 1;

    private long sequence;
    private String label;
    private long startTime;
    private long endTime;
    private String message;

    public PoolingQueueServiceStatistic(String label, long startTime, long endTime, String message) {
        this.sequence = globalSequence++;
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
    }

    public PoolingQueueServiceStatistic(String label, long startTime, long endTime) {
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
    /*** Sequence; StartTime; EndTime; TotalTime; Label; Message
         00005; normal; normal; 000005; normal; normal */

    public String mapToCSV(boolean messageSuppressed){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%06d; ", sequence));
        builder.append(String.format("%d; ", startTime));
        builder.append(String.format("%d; ", endTime));
        builder.append(String.format("%06d; ", (endTime-startTime)));
        builder.append(label + "; ");
        builder.append(message);

        return builder.toString();
    }

    @Override
    public String toString() {
        return mapToCSV(false);
    }
}
