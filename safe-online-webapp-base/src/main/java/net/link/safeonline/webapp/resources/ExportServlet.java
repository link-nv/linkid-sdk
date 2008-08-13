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

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.StatisticNotFoundException;
import net.link.safeonline.service.StatisticService;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class ExportServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(ExportServlet.class);

    @EJB(mappedName = "SafeOnline/StatisticServiceBean/local")
    private StatisticService  statisticService;


    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
    }

    @Override
    public void invokeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        response.setContentType("application/vnd.ms-excel");
        OutputStream out = response.getOutputStream();

        String chartName = request.getParameter("chartname");
        String domainName = request.getParameter("domain");
        String applicationName = request.getParameter("applicationname");

        LOG.debug("export: chartName=" + chartName + " domain=" + domainName + " applicationName=" + applicationName);

        if (null == applicationName)
            throw new ServletException("aplicationname request parameter missing");

        HSSFWorkbook workbook;

        if (null != chartName && null != domainName) {
            try {
                workbook = this.statisticService.exportStatistic(chartName, domainName, applicationName);
            } catch (StatisticNotFoundException e) {
                LOG.debug("Statistic not found: " + chartName + ", " + domainName + ", " + applicationName);
                throw new ServletException("Statistic not found: " + e.getMessage());
            }
        } else {
            try {
                workbook = this.statisticService.exportStatistics(applicationName);
            } catch (ApplicationNotFoundException e) {
                LOG.debug("application not found: " + applicationName);
                throw new ServletException(e.getMessage());
            } catch (StatisticNotFoundException e) {
                LOG.debug("statistic not found: " + e.getMessage());
                throw new ServletException("Statistic not found: " + e.getMessage());
            }
        }

        workbook.write(out);
        out.close();
    }

}
