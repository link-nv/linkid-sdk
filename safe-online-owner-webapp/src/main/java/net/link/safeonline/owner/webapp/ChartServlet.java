package net.link.safeonline.owner.webapp;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.owner.Charts;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.util.ee.EjbUtils;

public class ChartServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ChartServlet.class);

	private Charts charts;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadChartsBean();
	}

	private void loadChartsBean() {
		this.charts = EjbUtils.getEJB(OwnerConstants.JNDI_PREFIX
				+ "ChartsBean/local", Charts.class);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();

		String chartName = request.getParameter("chartname");
		String applicationName = request.getParameter("applicationname");

		byte[] buffer = null;
		try {
			LOG.debug("getting chart: " + chartName + " for application: "
					+ applicationName);
			buffer = charts.getChart(chartName, applicationName, 800, 600);
		} catch (Exception e) {
			LOG.debug("exception: " + e.getMessage());
			throw new ServletException();
		}
		if (buffer != null) {
			out.write(buffer);
		}

	}

}
