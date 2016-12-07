package br.com.pqs.statistics;

import java.util.List;

/**
 * Created by jordaoesa on 28/11/16.
 */
public interface IRequestStatisticallyProfilable {

    List<IStatistics> collectStatistics();

}
