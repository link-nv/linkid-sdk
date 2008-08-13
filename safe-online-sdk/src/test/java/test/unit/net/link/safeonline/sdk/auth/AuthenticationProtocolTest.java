/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth;

import static org.junit.Assert.assertEquals;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class AuthenticationProtocolTest {

    private static final Log LOG = LogFactory.getLog(AuthenticationProtocolTest.class);


    @Test
    public void literals() throws Exception {

        LOG.debug(AuthenticationProtocol.SAML2_BROWSER_POST);

        assertEquals("SAML2_BROWSER_POST", AuthenticationProtocol.SAML2_BROWSER_POST.name());
    }
}
