/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.p11sc.impl;

import junit.framework.TestCase;
import net.link.safeonline.p11sc.impl.SmartCardConfigImpl;


public class SmartCardConfigImplTest extends TestCase {

    private String              testAlias;

    private SmartCardConfigImpl testedInstance;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        testAlias = "test-alias-" + getName();

        testedInstance = new SmartCardConfigImpl(testAlias);
    }

    public void testGetAlias()
            throws Exception {

        // operate
        String result = testedInstance.getCardAlias();

        // verify
        assertEquals(testAlias, result);
    }

    public void testSetAuthAndSignKeyAliases()
            throws Exception {

        // setup
        String authKeyAlias = "test-auth-alias";
        String signKeyAlias = "test-sign-alias";

        // operate
        testedInstance.setAuthenticationKeyAlias(authKeyAlias);
        testedInstance.setSignatureKeyAlias(signKeyAlias);
        String authResult = testedInstance.getAuthenticationKeyAlias();
        String signResult = testedInstance.getSignatureKeyAlias();

        // verify
        assertEquals(authKeyAlias, authResult);
        assertEquals(signKeyAlias, signResult);
    }
}
