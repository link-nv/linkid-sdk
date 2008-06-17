/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.ErrorMessage;
import net.link.safeonline.sdk.servlet.annotation.Init;

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

	@Init(name = "ErrorPage", optional = true)
	private String errorPage;

	@Init(name = "ResourceBundle", optional = true)
	private String resourceBundleName;

	@Override
	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleLanding(request, response);
	}

	private void handleLanding(HttpServletRequest request,
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
			redirectToErrorPage(request, response, this.errorPage,
					this.resourceBundleName, new ErrorMessage(msg));

			return;
		}

		String username = protocolHandler.finalizeAuthentication(request,
				response);
		if (null == username) {
			String msg = "protocol handler could not finalize";
			LOG.error(msg);
			redirectToErrorPage(request, response, this.errorPage,
					this.resourceBundleName, new ErrorMessage(msg));
			return;
		}

		LOG.debug("username: " + username);
		LoginManager.setUsername(username, request);
		AuthenticationProtocolManager.cleanupAuthenticationHandler(request);
		String target = AuthenticationProtocolManager.getTarget(request);
		LOG.debug("target: " + target);
		response.sendRedirect(target);
	}
}
