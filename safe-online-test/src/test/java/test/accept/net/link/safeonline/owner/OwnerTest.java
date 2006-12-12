/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.owner;

import junit.framework.TestCase;
import test.accept.net.link.safeonline.AcceptanceTestManager;

public class OwnerTest extends TestCase {

	private AcceptanceTestManager acceptanceTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
		super.tearDown();
	}

	public void testOwnerLogonLogout() throws Exception {
		this.acceptanceTestManager.ownerLogon("owner", "secret");
		this.acceptanceTestManager.openOwnerWebApp("/applications.seam");
		this.acceptanceTestManager.logout();
	}
}
