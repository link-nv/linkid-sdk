/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CallbackServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(CallbackServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doGet");
		Enumeration headerNamesEnum = request.getHeaderNames();
		while (headerNamesEnum.hasMoreElements()) {
			String headerName = (String) headerNamesEnum.nextElement();
			String header = request.getHeader(headerName);
			LOG.debug("header: " + headerName + " = " + header);
		}
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			LOG
					.debug("cookie: " + cookie.getName() + " = "
							+ cookie.getValue());
		}

		String jsessionId = getJSessionId(request);
		LOG.debug("JSESSIONID = " + jsessionId);
	}

	private String getJSessionId(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if ("JSESSIONID".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
