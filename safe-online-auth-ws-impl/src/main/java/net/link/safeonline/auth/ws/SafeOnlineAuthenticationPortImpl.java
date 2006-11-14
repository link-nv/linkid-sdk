package net.link.safeonline.auth.ws;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.lin_k.safe_online.auth._1.FaultMessage;
import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort;

@WebService(endpointInterface = "net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationPort")
public class SafeOnlineAuthenticationPortImpl implements
		SafeOnlineAuthenticationPort {

	private static Log LOG = LogFactory
			.getLog(SafeOnlineAuthenticationPortImpl.class);

	public String echo(String request) throws FaultMessage {
		LOG.debug("echo: " + request);
		String result = request;
		return result;
	}
}
