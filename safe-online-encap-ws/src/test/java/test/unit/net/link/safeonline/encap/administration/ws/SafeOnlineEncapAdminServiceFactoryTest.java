/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.encap.administration.ws;

import http.BankIdAdministration;
import junit.framework.TestCase;
import net.link.safeonline.encap.administration.ws.SafeOnlineEncapAdministrationServiceFactory;

public class SafeOnlineEncapAdminServiceFactoryTest extends TestCase {

	public void testNewInstance() throws Exception {
		// operate
		BankIdAdministration result = SafeOnlineEncapAdministrationServiceFactory
				.newInstance();

		// verify
		assertNotNull(result);
	}
}
