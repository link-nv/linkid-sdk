package net.link.safeonline.auth.ws;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateRequestType;
import net.lin_k.safe_online.auth._1_0.types.AuthenticateResultType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebService(endpointInterface = "net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort")
public class SafeOnlineAuthenticationPortImpl implements
		SafeOnlineAuthenticationPort {

	private static Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationPortImpl.class);

	private static Map<String, String> authorizedUsers;

	static {
		authorizedUsers = new HashMap<String, String>();
		authorizedUsers.put("fcorneli", "secret");
		authorizedUsers.put("dieter", "secret");
		authorizedUsers.put("mario", "secret");
	}

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

		AuthenticateResultType result = new AuthenticateResultType();
		if (!authorizedUsers.containsKey(username)) {
			LOG.debug("unknown username: \"" + username + "\"");
			result.setAuthenticated(false);
			return result;
		}
		if (!authorizedUsers.get(username).equals(password)) {
			LOG.debug("incorrect password");
			result.setAuthenticated(false);
			return result;
		}
		LOG.debug("authorized user: " + username);
		result.setAuthenticated(true);
		return result;
	}
}
