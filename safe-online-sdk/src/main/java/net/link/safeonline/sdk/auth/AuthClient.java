package net.link.safeonline.sdk.auth;

import javax.xml.ws.BindingProvider;

import net.lin_k.safe_online.auth._1.FaultMessage;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;
import net.link.safeonline.auth.ws.SafeOnlineAuthenticationServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Authentication Web Sercice Client.
 * 
 * @author fcorneli
 * 
 */
public class AuthClient {

	private static final Log LOG = LogFactory.getLog(AuthClient.class);

	private SafeOnlineAuthenticationPort port;

	/**
	 * Main constructor.
	 * 
	 * @param location
	 *            the location (i.e. host:port) of the SafeOnline service.
	 */
	public AuthClient(String location) {
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
				"http://" + location + "/safe-online-ws/auth/");
	}

	/**
	 * Echos the given message argument. This operation can be used to test the
	 * availability of the SafeOnline authentication web service.
	 * 
	 * @param message
	 *            the message to echo.
	 * @return the echo of the message.
	 */
	public String echo(String message) {
		String result;
		try {
			result = this.port.echo(message);
		} catch (FaultMessage e) {
			throw new RuntimeException("Web Service Fault Message: "
					+ e.getMessage());
		}
		LOG.debug("echo result: " + result);
		return result;
	}
}
