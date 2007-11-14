/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.encap.authentication.ws;

import http.BankIdAuthentication;
import junit.framework.TestCase;
import net.link.safeonline.encap.authentication.SafeOnlineEncapAuthenticationServiceFactory;

public class SafeOnlineEncapAuthenticationServiceFactoryTest extends TestCase {

	public void testNewInstance() throws Exception {
		// operate
		BankIdAuthentication result = SafeOnlineEncapAuthenticationServiceFactory
				.newInstance();

		// verify
		assertNotNull(result);
	}
}