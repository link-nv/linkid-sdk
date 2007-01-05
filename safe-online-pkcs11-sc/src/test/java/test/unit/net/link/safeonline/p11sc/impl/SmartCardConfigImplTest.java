/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.p11sc.impl;

import javax.smartcardio.ATR;

import junit.framework.TestCase;
import net.link.safeonline.p11sc.impl.SmartCardConfigImpl;

public class SmartCardConfigImplTest extends TestCase {

	private String testAlias;

	private SmartCardConfigImpl testedInstance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testAlias = "test-alias-" + getName();

		this.testedInstance = new SmartCardConfigImpl(this.testAlias);
	}

	public void testATREqualsWorks() throws Exception {
		// setup
		ATR testAtr = new ATR(new byte[] { 0x01, 0x02, 0x03 });

		// operate
		this.testedInstance.addSupportedATR(testAtr);
		boolean result = this.testedInstance.isSupportedATR(testAtr);

		// verify
		assertTrue(result);
	}

	public void testGetAlias() throws Exception {
		// operate
		String result = this.testedInstance.getCardAlias();

		// verify
		assertEquals(this.testAlias, result);
	}

	public void testSetAuthAndSignKeyAliases() throws Exception {
		// setup
		String authKeyAlias = "test-auth-alias";
		String signKeyAlias = "test-sign-alias";

		// operate
		this.testedInstance.setAuthenticationKeyAlias(authKeyAlias);
		this.testedInstance.setSignatureKeyAlias(signKeyAlias);
		String authResult = this.testedInstance.getAuthenticationKeyAlias();
		String signResult = this.testedInstance.getSignatureKeyAlias();

		// verify
		assertEquals(authKeyAlias, authResult);
		assertEquals(signKeyAlias, signResult);
	}
}
