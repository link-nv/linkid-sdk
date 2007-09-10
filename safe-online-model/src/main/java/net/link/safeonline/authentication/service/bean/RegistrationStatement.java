/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import net.link.safeonline.authentication.exception.DecodingException;

/**
 * Registration Statement object class. Holds the structure and parser for the
 * client-side generated registration statement. Can also verify the signature
 * on the statement.
 * 
 * @author fcorneli
 * 
 */
public class RegistrationStatement extends
		AbstractStatement<RegistrationStatementStructure> {

	public RegistrationStatement(byte[] encodedRegistrationStatement)
			throws DecodingException {
		super(new RegistrationStatementStructure(encodedRegistrationStatement));
	}

	public String getSessionId() {
		return super.getStatementStructure().getSessionId();
	}

	public String getApplicationId() {
		return super.getStatementStructure().getApplicationId();
	}

	public String getUsername() {
		return super.getStatementStructure().getUsername();
	}
}
