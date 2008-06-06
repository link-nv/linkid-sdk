/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Login Servlet. This servlet contains the landing page to finalize the
 * authentication process initiated by the web application.
 * 
 * @author fcorneli
 * 
 */
public class LoginServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	public static final String ERROR_MESSAGE_ATTRIBUTE = "errorMessage";

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager
				.findAuthenticationProtocolHandler(request);
		if (null == protocolHandler) {
			/*
			 * The landing page can only be used for finalizing an ongoing
			 * authentication process. If no protocol handler is active then
			 * something must be going wrong here.
			 */
			String msg = "no protocol handler active";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}

		String username = protocolHandler.finalizeAuthentication(request,
				response);
		if (null == username) {
			String msg = "protocol handler could not finalize";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}

		LOG.debug("username: " + username);
		LoginManager.setUsername(username, request);
		AuthenticationProtocolManager.cleanupAuthenticationHandler(request);
		String target = AuthenticationProtocolManager.getTarget(request);
		LOG.debug("target: " + target);
		response.sendRedirect(target);
	}

	private void redirectToErrorPage(HttpServletRequest request,
			HttpServletResponse response, String errorMessage)
			throws IOException {
		HttpSession session = request.getSession();
		session.setAttribute(ERROR_MESSAGE_ATTRIBUTE, errorMessage);
		// response.sendRedirect(this.protocolErrorUrl);
	}

	// XXX: remove this by a custom jsf error page ( XPlanner: id=16262 )
	private void writeErrorPage(String message, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		PrintWriter out = response.getWriter();
		out.println("<html>");
		{
			out.println("<head><title>Error</title></head>");
			out.println("<body>");
			{
				out.println("<h1>Error</h1>");
				out.println("<p>");
				{
					out.println(message);
				}
				out.println("</p>");
			}
			out.println("</body>");
		}
		out.println("</html>");
		out.close();
	}
}
