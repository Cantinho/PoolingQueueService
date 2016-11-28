package br.com.example.statistics;

/**
 * Created by jordaoesa on 28/11/16.
 */
public interface IStatistics {

    long getSequence();

    String getLabel();

    String getMessage();

    long getTotalTime();

    String print(boolean messageSuppressed);

}
