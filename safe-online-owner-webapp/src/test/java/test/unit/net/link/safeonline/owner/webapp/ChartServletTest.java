/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.owner.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.owner.webapp.ChartServlet;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.joda.time.DateTime;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expect;

public class ChartServletTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(ChartServletTest.class);

	private ChartServlet testedInstance;

	private ServletTestManager servletTestManager;

	private String servletLocation;

	private JndiTestUtils jndiTestUtils;

	private StatisticService mockStatisticService;

	private Object[] mockObjects;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new ChartServlet();

		this.mockStatisticService = createMock(StatisticService.class);

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/StatisticServiceBean/local",
				this.mockStatisticService);

		this.servletTestManager = new ServletTestManager();
		this.servletLocation = this.servletTestManager
				.setUp(ChartServlet.class);

		this.mockObjects = new Object[] { this.mockStatisticService };
	}

	@Override
	protected void tearDown() throws Exception {
		this.servletTestManager.tearDown();
		this.jndiTestUtils.tearDown();

		super.tearDown();
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
		File file = File.createTempFile("tempchart-", ".png");
		FileOutputStream out = new FileOutputStream(file);
		out.write(ChartUtilities.encodeAsPNG(chart
				.createBufferedImage(800, 600)));
	}

	public void testDoGet() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletLocation);
		String testChartName = "test-chart-name-" + getName();
		String testApplicationName = "test-application-name-" + getName();
		getMethod.setQueryString(new NameValuePair[] {
				new NameValuePair("chartname", testChartName),
				new NameValuePair("applicationname", testApplicationName) });
		StatisticEntity statistic = new StatisticEntity();
		statistic.setName("test-statistic-name");
		statistic.getStatisticDataPoints().add(
				new StatisticDataPointEntity("test-data-point", statistic,
						new Date(), 1, 2, 3));
		statistic.getStatisticDataPoints().add(
				new StatisticDataPointEntity("test-data-point", statistic,
						new DateTime().plusDays(1).toDate(), 4, 5, 6));

		// stubs
		expect(
				this.mockStatisticService.getStatistic(testChartName,
						testApplicationName)).andStubReturn(statistic);

		// prepare
		replay(this.mockObjects);

		// operate
		int result = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		verify(this.mockObjects);
		assertEquals(HttpServletResponse.SC_OK, result);
		String resultContentType = getMethod.getResponseHeader("Content-Type")
				.getValue();
		LOG.debug("result content-type: " + resultContentType);
		assertEquals("image/png", resultContentType);
		LOG.debug("result content length: "
				+ getMethod.getResponseContentLength());

		File tmpFile = File.createTempFile("result-image-", ".png");
		FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
		IOUtils.copy(getMethod.getResponseBodyAsStream(), fileOutputStream);
		fileOutputStream.close();
	}
}
