/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.asn1.statement.DERIdentityStatement;


/**
 * Component for construction of the identity statement. The identity statement links the certificate with the username.
 * 
 * @author fcorneli
 * 
 */
public class IdentityStatement extends AbstractStatement {

    public IdentityStatement(String sessionId, String user, String operation, IdentityProvider identityProvider,
            Signer signer) {

        super(signer, new DERIdentityStatement(signer.getCertificate(), sessionId, user, operation, identityProvider
                .getGivenName(), identityProvider.getSurname()));
    }
}
