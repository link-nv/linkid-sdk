package net.link.safeonline.owner;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.StatisticEntity;

import org.jfree.chart.JFreeChart;

@Local
public interface Charts {

	void statListFactory() throws PermissionDeniedException;

	String viewStat();

	void destroyCallback();

	byte[] getChart(String chartName, String applicationName, int width,
			int height) throws StatisticNotFoundException,
			PermissionDeniedException;

	JFreeChart defaultChart(StatisticEntity statistic);

}
