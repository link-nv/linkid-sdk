/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.identity;

import junit.framework.TestCase;
import net.link.safeonline.identity.IdentityStatementFactory;
import net.link.safeonline.p11sc.SmartCard;

public class IdentityStatementFactoryTest extends TestCase {

	private IdentityStatementFactory testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.testedInstance = new IdentityStatementFactory();
	}

	public void testCreateIdentityStatement() throws Exception {
		// setup
		SmartCard testSmartCard = new TestSmartCard();

		// operate
		String result = this.testedInstance
				.createIdentityStatement(testSmartCard);

		// verify
		assertNull(result);
	}
}
