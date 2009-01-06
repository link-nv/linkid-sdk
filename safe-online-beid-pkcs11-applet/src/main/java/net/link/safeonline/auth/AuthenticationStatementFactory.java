/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.AuthenticationStatement;


public class AuthenticationStatementFactory {

    private AuthenticationStatementFactory() {

        // empty
    }

    public static byte[] createAuthenticationStatement(String sessionId, String applicationId, Signer signer) {

        AuthenticationStatement authenticationStatement = new AuthenticationStatement(sessionId, applicationId, signer);
        byte[] authenticationStatementData = authenticationStatement.generateStatement();
        return authenticationStatementData;
    }
}
