/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.asn1.statement.DERAuthenticationStatement;


/**
 * Component for constructing the authentication statement. The authentication statement links the session Id and
 * application Id with an PKIX-based authentication device.
 *
 * @author fcorneli
 *
 */
public class AuthenticationStatement extends AbstractStatement {

    public AuthenticationStatement(String sessionId, String applicationId, Signer signer) {

        super(signer, new DERAuthenticationStatement(sessionId, applicationId, signer.getCertificate()));
    }
}
