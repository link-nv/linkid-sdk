/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.auth.saml2.Saml2Handler;
import net.link.safeonline.device.sdk.exception.AuthenticationFinalizationException;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.sdk.servlet.ErrorMessage;
import net.link.safeonline.sdk.servlet.annotation.Init;

public class ExitServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	@Init(name = "ErrorPage", optional = true)
	private String errorPage;

	@Init(name = "ResourceBundle", optional = true)
	private String resourceBundleName;

	@Override
	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleExit(request, response);
	}

	@Override
	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleExit(request, response);
	}

	protected void handleExit(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Saml2Handler handler = Saml2Handler.findSaml2Handler(request);
		if (null == handler) {
			/*
			 * If no protocol handler is active at this point then something
			 * must be going wrong here.
			 */
			redirectToErrorPage(request, response, this.errorPage,
					this.resourceBundleName, new ErrorMessage(
							"errorNoProtocolHandlerActive"));
			return;

		}
		try {
			handler.finalizeAuthentication(request, response);
		} catch (AuthenticationFinalizationException e) {
			redirectToErrorPage(request, response, this.errorPage,
					this.resourceBundleName, new ErrorMessage(e.getMessage()));
		}
	}
}
