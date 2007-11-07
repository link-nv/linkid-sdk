/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.encap.activation.ws;

import http.BankIdActivation;
import junit.framework.TestCase;
import net.link.safeonline.encap.activation.ws.SafeOnlineEncapActivationServiceFactory;

public class SafeOnlineEncapActivationServiceFactoryTest extends TestCase {

	public void testNewInstance() throws Exception {
		// operate
		BankIdActivation result = SafeOnlineEncapActivationServiceFactory
				.newInstance();

		// verify
		assertNotNull(result);
	}
}
