/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth;

import static org.junit.Assert.assertNotNull;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolHandler;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;

import org.junit.Test;

public class AuthenticationProtocolManagerTest {

	@Test
	public void simpleProtocolHandler() throws Exception {
		// operate
		AuthenticationProtocolHandler simpleAuthenticationProtocolHandler = AuthenticationProtocolManager
				.getAuthenticationProtocolHandler(
						AuthenticationProtocol.SIMPLE_PLAIN_URL,
						"http://authn.service", "app-name", null, null);

		// verify
		assertNotNull(simpleAuthenticationProtocolHandler);
	}

	@Test
	public void saml2ProtocolHandler() throws Exception {
		// operate
		AuthenticationProtocolHandler saml2AuthenticationProtocolHandler = AuthenticationProtocolManager
				.getAuthenticationProtocolHandler(
						AuthenticationProtocol.SAML2_BROWSER_POST,
						"http://authn.service", "application-id", null, null);

		// verify
		assertNotNull(saml2AuthenticationProtocolHandler);
	}
}
