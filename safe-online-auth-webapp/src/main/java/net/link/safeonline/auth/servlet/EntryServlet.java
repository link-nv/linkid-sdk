/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.protocol.ProtocolHandler;
import net.link.safeonline.auth.protocol.SimpleProtocolHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic entry point for the authentication web application. This servlet will
 * try to find out which authentication protocol is being used by the client web
 * browser to initiate an authentication procedure. We manage the authentication
 * entry via a bare-bone servlet since we need to be able to do some low-level
 * GET or POST parsing.
 * 
 * @author fcorneli
 * 
 */
public class EntryServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(EntryServlet.class);

	private static final long serialVersionUID = 1L;

	private static final List<ProtocolHandler> protocolHandlers = new LinkedList<ProtocolHandler>();

	static {
		registerProtocolHandler(SimpleProtocolHandler.class);
	}

	private static void registerProtocolHandler(
			Class<? extends ProtocolHandler> protocolHandlerClass) {
		try {
			ProtocolHandler protocolHandler = protocolHandlerClass
					.newInstance();
			protocolHandlers.add(protocolHandler);
		} catch (Exception e) {
			throw new RuntimeException(
					"could not initialize protocol handler: "
							+ protocolHandlerClass.getName());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("GET entry");
		handleEntry(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("POST entry");
		handleEntry(request, response);
	}

	private void handleEntry(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		for (ProtocolHandler protocolHandler : protocolHandlers) {
			LOG.debug("trying protocol handler: "
					+ protocolHandler.getClass().getSimpleName());
			String applicationId = protocolHandler.handleRequest(request);
			if (null != applicationId) {
				String protocolName = protocolHandler.getName();
				LOG.debug("authentication protocol: " + protocolName);
				HttpSession session = request.getSession();
				session.setAttribute("applicationId", applicationId);
				response.sendRedirect("./main.seam");
				return;
			}
		}
		/*
		 * Else no appropriate protocol handler was found.
		 */
		response.sendRedirect("./unsupported-protocol.seam");
	}
}
