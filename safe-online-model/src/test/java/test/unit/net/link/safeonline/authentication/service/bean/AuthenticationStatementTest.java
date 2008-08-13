/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.bean.AuthenticationStatement;
import net.link.safeonline.shared.JceSigner;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.test.util.PkiTestUtils;


public class AuthenticationStatementTest extends TestCase {

    public void testVerify() throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        String sessionId = UUID.randomUUID().toString();
        String applicationId = "test-application-id";

        Signer signer = new JceSigner(keyPair.getPrivate(), certificate);

        net.link.safeonline.shared.statement.AuthenticationStatement testAuthenticationStatement = new net.link.safeonline.shared.statement.AuthenticationStatement(
                sessionId, applicationId, signer);

        byte[] encodedAuthenticationStatement = testAuthenticationStatement.generateStatement();

        // operate
        AuthenticationStatement authenticationStatement = new AuthenticationStatement(encodedAuthenticationStatement);

        X509Certificate resultCertificate = authenticationStatement.verifyIntegrity();

        // verify
        assertNotNull(resultCertificate);
        assertEquals(certificate, resultCertificate);
        assertEquals(sessionId, authenticationStatement.getSessionId());
        assertEquals(applicationId, authenticationStatement.getApplicationId());
    }
}
