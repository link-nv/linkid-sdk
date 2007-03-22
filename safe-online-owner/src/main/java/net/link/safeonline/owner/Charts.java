package net.link.safeonline.owner;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.entity.StatisticEntity;

import org.jfree.chart.JFreeChart;

@Local
public interface Charts {

	byte[] getChart(String chartName, String applicationName, int width,
			int height) throws StatisticNotFoundException;

	public JFreeChart defaultChart(StatisticEntity statistic);

}
