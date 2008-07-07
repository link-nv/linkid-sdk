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
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
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

	@Init(name = "ServletEndpointUrl", optional = true)
	private String servletEndpointUrl;

	@Init(name = "ErrorPage", optional = true)
	private String errorPage;

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

		/**
		 * Wrap the request to use the servlet endpoint url if defined. To
		 * prevent failure when behind a reverse proxy or loadbalancer when
		 * opensaml is checking the destination field.
		 */
		HttpServletRequestEndpointWrapper requestWrapper;
		if (null != this.servletEndpointUrl) {
			requestWrapper = new HttpServletRequestEndpointWrapper(request,
					this.servletEndpointUrl);
		} else {
			requestWrapper = new HttpServletRequestEndpointWrapper(request,
					request.getRequestURL().toString());
		}

		AuthenticationProtocolHandler protocolHandler = AuthenticationProtocolManager
				.findAuthenticationProtocolHandler(requestWrapper);
		if (null == protocolHandler) {
			/*
			 * The landing page can only be used for finalizing an ongoing
			 * authentication process. If no protocol handler is active then
			 * something must be going wrong here.
			 */
			String msg = "no protocol handler active";
			LOG.error(msg);
			redirectToErrorPage(requestWrapper, response, this.errorPage, null,
					new ErrorMessage(msg));

			return;
		}

		String username = protocolHandler.finalizeAuthentication(
				requestWrapper, response);
		if (null == username) {
			String msg = "protocol handler could not finalize";
			LOG.error(msg);
			redirectToErrorPage(requestWrapper, response, this.errorPage, null,
					new ErrorMessage(msg));
			return;
		}

		LOG.debug("username: " + username);
		LoginManager.setUsername(username, requestWrapper);
		AuthenticationProtocolManager
				.cleanupAuthenticationHandler(requestWrapper);
		String target = AuthenticationProtocolManager.getTarget(requestWrapper);
		LOG.debug("target: " + target);
		response.sendRedirect(target);
	}
}
