/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import net.link.safeonline.authentication.exception.DecodingException;

public class IdentityStatement extends
		AbstractStatement<IdentityStatementStructure> {

	public IdentityStatement(byte[] encodedIdentityStatement)
			throws DecodingException {
		super(new IdentityStatementStructure(encodedIdentityStatement));
	}

	public String getGivenName() {
		return super.getStatementStructure().getGivenName();
	}

	public String getSurname() {
		return super.getStatementStructure().getSurname();
	}

	public String getUser() {
		return super.getStatementStructure().getUser();
	}
}
