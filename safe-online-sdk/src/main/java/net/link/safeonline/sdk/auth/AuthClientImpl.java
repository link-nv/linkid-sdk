package net.link.safeonline.sdk.auth;

import javax.xml.ws.BindingProvider;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of authentication client.
 * 
 * @author fcorneli
 * 
 */
public class AuthClientImpl implements AuthClient {

	private static final Log LOG = LogFactory.getLog(AuthClientImpl.class);

	private SafeOnlineAuthenticationPort port;

	/**
	 * Main constructor.
	 * 
	 * @param location
	 *            the location (i.e. host:port) of the SafeOnline service.
	 */
	public AuthClientImpl(String location) {
		LOG.debug("auth client: " + location);

		SafeOnlineAuthenticationService service = SafeOnlineAuthenticationServiceFactory
				.newInstance();

		this.port = service.getSafeOnlineAuthenticationPort();

		setEndpointAddress(location);
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://" + location + "/safe-online-ws/auth");
	}

	public String echo(String message) {
		String result = this.port.echo(message);
		LOG.debug("echo result: " + result);
		return result;
	}

	public boolean authenticate(String username, String password) {
		AuthenticateRequestType request = new AuthenticateRequestType();
		request.setUsername(username);
		request.setPassword(password);
		LOG.debug("authentication request for user: " + username);
		AuthenticateResultType result = this.port.authenticate(request);
		boolean authenticated = result.isAuthenticated();
		LOG.debug("authentication result: " + authenticated);
		return authenticated;
	}
}
