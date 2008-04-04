/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.user;

import net.link.safeonline.webapp.AcceptanceTestManager;
import junit.framework.TestCase;

/**
 * Acceptance test for user web application.
 * 
 * @author fcorneli
 * 
 */
public class UserTest extends TestCase {

	private AcceptanceTestManager acceptanceTestManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
		super.tearDown();
	}

}
