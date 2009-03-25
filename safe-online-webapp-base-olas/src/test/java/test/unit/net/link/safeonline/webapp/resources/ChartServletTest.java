/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.resources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.webapp.resources.ChartServlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.joda.time.DateTime;


public class ChartServletTest extends TestCase {

    private static final Log   LOG = LogFactory.getLog(ChartServletTest.class);

    private ServletTestManager servletTestManager;

    private String             servletLocation;

    private JndiTestUtils      jndiTestUtils;

    private StatisticService   mockStatisticService;

    private Object[]           mockObjects;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        mockStatisticService = createMock(StatisticService.class);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("SafeOnline/StatisticServiceBean/local", mockStatisticService);

        servletTestManager = new ServletTestManager();
        servletTestManager.setUp(ChartServlet.class);
        servletLocation = servletTestManager.getServletLocation();
        SafeOnlineTestConfig.loadTest(servletTestManager);

        mockObjects = new Object[] { mockStatisticService };
    }

    @Override
    protected void tearDown()
            throws Exception {

        servletTestManager.tearDown();
        jndiTestUtils.tearDown();

        super.tearDown();
    }

    public void testDoGet()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(servletLocation);
        String testChartName = "test-chart-name-" + getName();
        String testDomain = "test-domain-" + getName();
        String testApplicationName = "test-application-name-" + getName();
        getMethod.setQueryString(new NameValuePair[] { new NameValuePair("chartname", testChartName),
                new NameValuePair("domain", testDomain), new NameValuePair("applicationname", testApplicationName) });
        StatisticEntity statistic = new StatisticEntity();
        statistic.setName("test-statistic-name");
        statistic.getStatisticDataPoints().add(new StatisticDataPointEntity("test-data-point", statistic, new Date(), 1, 2, 3));
        statistic.getStatisticDataPoints().add(
                new StatisticDataPointEntity("test-data-point", statistic, new DateTime().plusDays(1).toDate(), 4, 5, 6));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(statistic.getName(), // chart
                // title
                "Category", // domain axis label
                "Value", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
                                       );

        // stubs

        expect(mockStatisticService.getChart(testChartName, testDomain, testApplicationName)).andStubReturn(chart);

        // prepare
        replay(mockObjects);

        // operate
        int result = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result: " + result);
        verify(mockObjects);
        assertEquals(HttpServletResponse.SC_OK, result);
        String resultContentType = getMethod.getResponseHeader("Content-Type").getValue();
        LOG.debug("result content-type: " + resultContentType);
        assertEquals("image/png", resultContentType);
        LOG.debug("result content length: " + getMethod.getResponseContentLength());

        File tmpFile = File.createTempFile("result-image-", ".png");
        FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
        IOUtils.copy(getMethod.getResponseBodyAsStream(), fileOutputStream);
        fileOutputStream.close();
    }
}
