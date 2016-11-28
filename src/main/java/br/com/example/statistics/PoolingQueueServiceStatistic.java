package br.com.example.statistics;

/**
 * Created by jordaoesa on 28/11/16.
 */
public class PoolingQueueServiceStatistic implements IStatistics {

    private static long globalSequence = 1;

    private long sequence;
    private String label;
    private long totalTime;
    private String message;

    public PoolingQueueServiceStatistic(String label, long totalTime, String message) {
        this.sequence = globalSequence++;
        this.label = label;
        this.totalTime = totalTime;
        this.message = message;
    }

    public PoolingQueueServiceStatistic(String label, long totalTime) {
        this(label, totalTime, "");
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
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public String print(boolean messageSuppressed) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getName() + ":[");
        stringBuilder.append("sequence:" + sequence + ";");
        stringBuilder.append("label:" + label + ";");
        stringBuilder.append("totalTime:" + totalTime + ";");
        if(!messageSuppressed) stringBuilder.append("message:" + message);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return print(false);
    }
}
