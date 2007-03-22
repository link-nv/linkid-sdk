package test.unit.net.link.safeonline.owner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Random;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.owner.bean.ChartsBean;
import junit.framework.TestCase;

public class ChartsBeanTest extends TestCase {

	private ChartsBean testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.testedInstance = new ChartsBean();
	}

	public void testChartGeneration() throws Exception {
		// setup
		StatisticEntity statistic = new StatisticEntity("Test Stat", null,
				new Date());
		Random generator = new Random();
		StatisticDataPointEntity dp = new StatisticDataPointEntity("Cat A",
				statistic, new Date(), generator.nextInt(),
				generator.nextInt(), generator.nextInt());
		statistic.getStatisticDataPoints().add(dp);
		dp = new StatisticDataPointEntity("Cat B", statistic, new Date(),
				generator.nextInt(), generator.nextInt(), generator.nextInt());
		statistic.getStatisticDataPoints().add(dp);
		dp = new StatisticDataPointEntity("Cat C", statistic, new Date(),
				generator.nextInt(), generator.nextInt(), generator.nextInt());
		statistic.getStatisticDataPoints().add(dp);
		dp = new StatisticDataPointEntity("Cat D", statistic, new Date(),
				generator.nextInt(), generator.nextInt(), generator.nextInt());
		statistic.getStatisticDataPoints().add(dp);

		// operate
		JFreeChart chart = this.testedInstance.defaultChart(statistic);

		// verify
		File file = File.createTempFile("tempchart", ".png");
		FileOutputStream out = new FileOutputStream(file);
		out.write(ChartUtilities.encodeAsPNG(chart
				.createBufferedImage(800, 600)));

	}

}
