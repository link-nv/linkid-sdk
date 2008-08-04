/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract statement servlet. Helpdesk events are handled by the helpdesk
 * servlet.
 * 
 * @author wvdhaute
 * @see HelpdeskServlet
 * 
 */
public abstract class AbstractStatementServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AbstractStatementServlet.class);

	protected abstract void processStatement(byte[] statementData,
			HttpSession session, HttpServletResponse response)
			throws ServletException, IOException;

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			LOG.debug("content type: " + contentType);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		byte[] statementData = extractStatement(request);
		if (null == statementData) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		HttpSession session = request.getSession();

		processStatement(statementData, session, response);
	}

	private byte[] extractStatement(HttpServletRequest request)
			throws IOException {
		int size = request.getContentLength();
		if (0 == size) {
			LOG.debug("no statement present");
			return null;
		}
		InputStream contentInputStream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(contentInputStream, outputStream);
		byte[] statementData = outputStream.toByteArray();
		return statementData;
	}
}
