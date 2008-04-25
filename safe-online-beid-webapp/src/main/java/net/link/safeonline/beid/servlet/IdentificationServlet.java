/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Entry-point for identification web application service.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationServlet extends HttpServlet {

	private static final Log LOG = LogFactory
			.getLog(IdentificationServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String targetParameter = request.getParameter("target");
		if (null == targetParameter) {
			response.sendRedirect("identification.seam");
		} else {
			LOG.debug("target: " + targetParameter);
			HttpSession session = request.getSession();
			session.setAttribute("target", targetParameter);
			response.sendRedirect("identification-pcsc.seam");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
