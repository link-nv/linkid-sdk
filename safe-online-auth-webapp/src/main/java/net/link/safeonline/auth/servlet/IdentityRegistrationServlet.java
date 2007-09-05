/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The identity servlet implementation. This servlet receives its data from the
 * BeID via the IdentityApplet. Via this data web will register the user within
 * SafeOnline.
 * 
 * @author fcorneli
 * 
 */
public class IdentityRegistrationServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(IdentityRegistrationServlet.class);

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		InputStream contentInputStream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(contentInputStream, outputStream);
		byte[] identityStatementData = outputStream.toByteArray();

		HttpSession session = request.getSession();
		session.setAttribute("IdentityStatement", identityStatementData);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
