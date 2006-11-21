package net.link.safeonline.auth.ws;

import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;
import net.link.safeonline.authentication.service.AuthenticationService;

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
		String username = request.getUsername();
		String password = request.getPassword();

		AuthenticationService authenticationService = getService();
		boolean serviceResult = authenticationService.authenticate(username,
				password);

		AuthenticateResultType result = new AuthenticateResultType();
		result.setAuthenticated(serviceResult);
		return result;
	}

	private AuthenticationService getService() {
		AuthenticationService authenticationService = getEJB(
				"SafeOnline/AuthenticationServiceBean/local",
				AuthenticationService.class);
		return authenticationService;
	}

	@SuppressWarnings("unchecked")
	private <Type> Type getEJB(String jndiName, Class<Type> type) {
		try {
			LOG.debug("ejb jndi lookup: " + jndiName);
			InitialContext initialContext = new InitialContext();
			Object obj = initialContext.lookup(jndiName);
			if (!type.isInstance(obj)) {
				throw new RuntimeException(jndiName + " is not a "
						+ type.getName() + " but a " + obj.getClass().getName());
			}
			Type instance = (Type) obj;
			return instance;
		} catch (NamingException e) {
			throw new RuntimeException("naming error: " + e.getMessage(), e);
		}
	}
}
