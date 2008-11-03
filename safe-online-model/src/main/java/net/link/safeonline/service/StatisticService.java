/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.StatisticEntity;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jfree.chart.JFreeChart;


@Local
public interface StatisticService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/StatisticServiceBean/local";

    public StatisticEntity getStatistic(String statisticName, String statisticDomain, String applicationName)
                                                                                                             throws StatisticNotFoundException,
                                                                                                             PermissionDeniedException;

    public List<StatisticEntity> getStatistics(ApplicationEntity application) throws PermissionDeniedException;

    public JFreeChart getChart(String statisticName, String statisticDomain, String applicationName) throws StatisticNotFoundException;

    public HSSFWorkbook exportStatistic(String statisticName, String statisticDomain, String applicationName)
                                                                                                             throws StatisticNotFoundException;

    public HSSFWorkbook exportStatistics(String applicationName) throws ApplicationNotFoundException, StatisticNotFoundException;
}
