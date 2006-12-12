/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import javax.jws.WebService;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebService(endpointInterface = "net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort")
public class SafeOnlineAuthenticationPortImpl implements
		SafeOnlineAuthenticationPort {

	private static Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationPortImpl.class);

	public SafeOnlineAuthenticationPortImpl() {
		LOG.debug("ready");
	}

	public String echo(String request) {
		LOG.debug("echo: " + request);
		String result = request;
		return result;
	}

	public AuthenticateResultType authenticate(AuthenticateRequestType request) {
		LOG.debug("authenticate");
		String application = request.getApplication();
		String username = request.getUsername();
		String password = request.getPassword();

		AuthenticationService authenticationService = getService();
		boolean serviceResult = authenticationService.authenticate(application,
				username, password);

		AuthenticateResultType result = new AuthenticateResultType();
		result.setAuthenticated(serviceResult);
		result.setApplication(application);
		result.setUsername(username);
		return result;
	}

	private AuthenticationService getService() {
		AuthenticationService authenticationService = EjbUtils.getEJB(
				"SafeOnline/AuthenticationServiceBean/local",
				AuthenticationService.class);
		return authenticationService;
	}
}
