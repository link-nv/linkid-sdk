/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.webapp.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CinemaTicketServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(CinemaTicketServlet.class);

	public static final String NRN_PARAMETER = "NRN";

	public static final String TIME_PARAMETER = "TIME";

	private TicketService ticketService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.debug("init");
		this.ticketService = getTicketService();
	}

	private TicketService getTicketService() {
		return EjbUtils
				.getEJB(TicketService.LOCAL_BINDING, TicketService.class);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("do get");
		String nrn = request.getParameter(NRN_PARAMETER);
		if (null == nrn) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No National Register Number passed as parameter.");
			return;
		}

		String time = request.getParameter(TIME_PARAMETER);
		if (null == time) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No TIME parameter");
			return;
		}

		boolean result = this.ticketService.hasValidPass(nrn, time);

		if (false == result) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"User has no valid pass.");
		}
	}
}
