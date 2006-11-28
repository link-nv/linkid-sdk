package net.link.safeonline.auth.ws;

import java.net.URL;

import javax.xml.namespace.QName;

import net.lin_k.safe_online.auth._1.SafeOnlineAuthenticationService;

/**
 * Factory for SafeOnlineAuthenticationService.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineAuthenticationServiceFactory {

	private SafeOnlineAuthenticationServiceFactory() {
		// empty
	}

	/**
	 * Creates a new instance of the SafeOnline authentication JAX-WS service
	 * stub.
	 * 
	 * @return a new instance.
	 */
	public static SafeOnlineAuthenticationService newInstance() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		URL wsdlUrl = classLoader.getResource("safe-online-auth.wsdl");
		if (null == wsdlUrl) {
			throw new RuntimeException("safe online WSDL not found");
		}

		SafeOnlineAuthenticationService service = new SafeOnlineAuthenticationService(
				wsdlUrl, new QName("urn:net:lin-k:safe-online:auth:1.0",
						"SafeOnlineAuthenticationService"));

		return service;
	}
}
