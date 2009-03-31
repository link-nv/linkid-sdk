/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pkcs11.identity;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;
import net.link.safeonline.shared.statement.IdentityStatement;


/**
 * A factory for identity statements.
 * 
 * @author fcorneli
 * 
 */
public class IdentityStatementFactory {

    private IdentityStatementFactory() {

        // empty
    }

    /**
     * Creates a new identity statement linking the user with the given smart card.
     * 
     * @param user
     *            the Id of the user.
     * @param signer
     * @param identityProvider
     * @return the ASN.1 DER encoded identity statement.
     */
    public static byte[] createIdentityStatement(String sessionId, String user, String operation, Signer signer,
                                                 IdentityProvider identityProvider) {

        if (null == sessionId)
            throw new IllegalArgumentException("sessionId should not be null");
        if (null == user)
            throw new IllegalArgumentException("user should not be null");
        if (null == operation)
            throw new IllegalArgumentException("operation should not be null");
        IdentityStatement identityStatement = new IdentityStatement(sessionId, user, operation, identityProvider, signer);
        byte[] identityStatementData = identityStatement.generateStatement();
        return identityStatementData;
    }
}
