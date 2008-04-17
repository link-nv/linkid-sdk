/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.reg;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.RegistrationStatement;

public class RegistrationStatementFactory {

	private RegistrationStatementFactory() {
		// empty
	}

	public static byte[] createRegistrationStatement(String user,
			String sessionId, String applicationId, Signer signer) {
		RegistrationStatement registrationStatement = new RegistrationStatement(
				user, sessionId, applicationId, signer);
		byte[] registrationStatementData = registrationStatement
				.generateStatement();
		return registrationStatementData;
	}
}
