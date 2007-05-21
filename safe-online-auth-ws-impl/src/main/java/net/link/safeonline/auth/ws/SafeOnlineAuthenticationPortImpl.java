/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.ws.soap.Addressing;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

@WebService(endpointInterface = "net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort")
@Addressing
public class SafeOnlineAuthenticationPortImpl implements
		SafeOnlineAuthenticationPort {

	private static Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationPortImpl.class);

	@PostConstruct
	public void postConstructCallback() {
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

		/*
		 * Keep in mind that the authentication service is a stateful session
		 * bean.
		 */
		AuthenticationService authenticationService = getService();
		boolean serviceResult;
		try {
			serviceResult = authenticationService.authenticate(username,
					password);
		} catch (SubjectNotFoundException e) {
			serviceResult = false;
		}

		if (false == serviceResult) {
			authenticationService.abort();
		} else {
			try {
				/*
				 * Notice that commitAuthentication can only take place when the
				 * user is already authenticated for the SafeOnline core. Thus
				 * authentication also propagates from the SafeOnline core
				 * towards outer applications.
				 */
				LoginContext loginContext = login(username);
				try {
					authenticationService.commitAuthentication(application);
				} finally {
					logout(loginContext);
				}
			} catch (SubscriptionNotFoundException e) {
				serviceResult = false;
			} catch (ApplicationNotFoundException e) {
				serviceResult = false;
			} catch (ApplicationIdentityNotFoundException e) {
				serviceResult = false;
			} catch (IdentityConfirmationRequiredException e) {
				serviceResult = false;
			} catch (MissingAttributeException e) {
				serviceResult = false;
			} catch (LoginException e) {
				serviceResult = false;
			}
		}

		AuthenticateResultType result = new AuthenticateResultType();
		result.setAuthenticated(serviceResult);
		result.setApplication(application);
		result.setUsername(username);
		return result;
	}

	private LoginContext login(String username) throws LoginException {
		UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
				null);
		LoginContext loginContext = new LoginContext("client-login", handler);
		loginContext.login();
		return loginContext;
	}

	private void logout(LoginContext loginContext) throws LoginException {
		loginContext.logout();
	}

	private AuthenticationService getService() {
		AuthenticationService authenticationService = EjbUtils.getEJB(
				"SafeOnline/AuthenticationServiceBean/local",
				AuthenticationService.class);
		return authenticationService;
	}
}
