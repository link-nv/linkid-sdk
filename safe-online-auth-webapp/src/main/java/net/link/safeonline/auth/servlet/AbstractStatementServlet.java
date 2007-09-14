/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
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

import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.HelpdeskCodes;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract statement servlet. Looks for helpdesk headers and accordingly
 * generates the required calls to HelpdeskLogger
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractStatementServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(AbstractStatementServlet.class);

	protected abstract void processStatement(byte[] statementData,
			HttpSession session, HttpServletResponse response)
			throws ServletException, IOException;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			LOG.debug("content type: " + contentType);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		boolean hasHelpdeskHeaders = processHelpdeskHeaders(request, response);
		if (true == hasHelpdeskHeaders) {
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

	private boolean processHelpdeskHeaders(HttpServletRequest request,
			HttpServletResponse response) {
		if (null == request.getHeader(HelpdeskCodes.HELPDESK_START)) {
			return false;
		}
		LOG.debug("request has helpdesk events attached ...");

		if (null != request.getHeader(HelpdeskCodes.HELPDESK_CLEAR)) {
			HelpdeskLogger.clear(request.getSession());
		}

		if (null != request.getHeader(HelpdeskCodes.HELPDESK_ADD)) {
			String message = request
					.getHeader(HelpdeskCodes.HELPDESK_ADD_MESSAGE);
			String logLevelString = request
					.getHeader(HelpdeskCodes.HELPDESK_ADD_LEVEL);
			if (null == message || null == logLevelString) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			LogLevelType logLevel = LogLevelType.valueOf(logLevelString);

			HelpdeskLogger.add(request.getSession(), message, logLevel);
		}

		if (null != request.getHeader(HelpdeskCodes.HELPDESK_PERSIST)) {
			String location = request
					.getHeader(HelpdeskCodes.HELPDESK_PERSIST_LOCATION);
			if (null == location)
				location = "unknown";

			Long id = HelpdeskLogger.persistContext(location, request
					.getSession());

			response.setHeader(HelpdeskCodes.HELPDESK_PERSIST_RETURN_ID, id
					.toString());
		}
		return true;
	}
}
