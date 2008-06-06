/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.resources;

import java.io.IOException;
import java.io.OutputStream;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.service.StatisticService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

public class ChartServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ChartServlet.class);

	@EJB(mappedName = "SafeOnline/StatisticServiceBean/local")
	private StatisticService statisticService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.securityCheck = false;
	}

	@Override
	public void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();

		String chartName = request.getParameter("chartname");
		String domainName = request.getParameter("domain");
		String applicationName = request.getParameter("applicationname");

		if (null == chartName) {
			throw new ServletException("chartname request parameter missing");
		}
		if (null == applicationName) {
			throw new ServletException(
					"aplicationname request parameter missing");
		}

		LOG.debug("getting chart: " + chartName + " for application: "
				+ applicationName);

		byte[] buffer = null;
		try {
			buffer = getChart(chartName, domainName, applicationName, 800, 600);
		} catch (Exception e) {
			LOG.debug("exception: " + e.getMessage());
			throw new ServletException(e.getMessage(), e);
		}
		if (buffer != null) {
			out.write(buffer);
		}

	}

	public byte[] getChart(String chartName, String domainName,
			String applicationName, int width, int heigth)
			throws StatisticNotFoundException {
		LOG.debug("finding statistic: " + chartName + " for application: "
				+ applicationName);
		JFreeChart chart = this.statisticService.getChart(chartName,
				domainName, applicationName);

		byte[] result = null;

		try {
			result = ChartUtilities.encodeAsPNG(chart.createBufferedImage(
					width, heigth));
		} catch (Exception e) {
			LOG.debug("Could not generate image");
			return null;
		}

		LOG.debug("returning byte[]");
		return result;
	}
}
