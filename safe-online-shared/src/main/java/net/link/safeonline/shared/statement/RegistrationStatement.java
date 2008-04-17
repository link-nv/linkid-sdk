/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.asn1.statement.DERRegistrationStatement;

/**
 * Component for the construction of the registration statement. The
 * registration statements links the following items together: user, session,
 * application and certificate.
 * 
 * @author fcorneli
 * 
 */
public class RegistrationStatement extends AbstractStatement {

	public RegistrationStatement(String user, String sessionId,
			String applicationId, Signer signer) {
		super(signer, new DERRegistrationStatement(user, sessionId,
				applicationId, signer.getCertificate()));
	}
}
