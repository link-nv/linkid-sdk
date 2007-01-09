/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IdentityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(IdentityServlet.class);

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		LOG.debug("content type: " + contentType);
		if (false == "text/xml".equals(contentType)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		response.setContentType("text/plain");

		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.println("Hello World");
		writer.close();
	}
}
