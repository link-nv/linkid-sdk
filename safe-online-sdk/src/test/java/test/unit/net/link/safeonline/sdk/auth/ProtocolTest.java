/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth;

import static org.junit.Assert.*;

import net.link.safeonline.sdk.auth.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class ProtocolTest {

    private static final Log LOG = LogFactory.getLog( ProtocolTest.class );

    @Test
    public void literals()
            throws Exception {

        LOG.debug( Protocol.SAML2 );
        LOG.debug( Protocol.OPENID );

        assertEquals( "SAML2", Protocol.SAML2.name() );
        assertEquals( "OPENID", Protocol.OPENID.name() );
    }
}
