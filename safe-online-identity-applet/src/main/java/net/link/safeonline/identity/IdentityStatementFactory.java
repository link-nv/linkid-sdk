/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

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
	 * Creates a new identity statement linking the user with the given smart
	 * card.
	 * 
	 * @param user
	 *            the Id of the user.
	 * @param signer
	 * @param identityProvider
	 * @return the ASN.1 DER encoded identity statement.
	 */
	public static byte[] createIdentityStatement(String user, Signer signer,
			IdentityProvider identityProvider) {
		IdentityStatement identityStatement = new IdentityStatement(user,
				identityProvider, signer);
		byte[] identityStatementData = identityStatement.generateStatement();
		return identityStatementData;
	}
}
