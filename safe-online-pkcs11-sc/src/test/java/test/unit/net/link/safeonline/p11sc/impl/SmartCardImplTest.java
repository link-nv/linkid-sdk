/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.p11sc.impl;

import junit.framework.TestCase;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardFactory;

public class SmartCardImplTest extends TestCase {

	private SmartCard testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = SmartCardFactory.newInstance();
	}

	public void testIsCardPresentRequiresInit() throws Exception {
		// operate & verify
		try {
			this.testedInstance.isSupportedCardPresent();
			fail();
		} catch (IllegalStateException e) {
			// expected
		}
	}
}
