/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.encap.authentication.ws;

import junit.framework.TestCase;
import net.link.safeonline.encap.authentication.SafeOnlineEncapAuthenticationServiceFactory;
import _0._1.authentication.encap.safe_online.link.net.BankIdAuthentication;

public class SafeOnlineEncapAuthenticationServiceFactoryTest extends TestCase {

	public void testNewInstance() throws Exception {
		// operate
		BankIdAuthentication result = SafeOnlineEncapAuthenticationServiceFactory
				.newInstance();

		// verify
		assertNotNull(result);
	}
}
