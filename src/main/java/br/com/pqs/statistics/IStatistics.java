package br.com.pqs.statistics;

/**
 * Created by jordaoesa on 28/11/16.
 */
public interface IStatistics {

    long getSequence();

    String getLabel();

    String getMessage();

    long getStartTime();

    long getEndTime();

    long getTotalTime();

    String print(boolean messageSuppressed);

    String csv(boolean messageSuppressed);
}
