/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;

public class SamlResponseMessageContext extends
		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> {

	private final String expectedInResponseTo;

	private final String expectedApplicationName;

	public SamlResponseMessageContext(String expectedInResponseTo,
			String expectedApplicationName) {
		this.expectedInResponseTo = expectedInResponseTo;
		this.expectedApplicationName = expectedApplicationName;
	}

	public String getExpectedInResponseTo() {
		return this.expectedInResponseTo;
	}

	public String getExpectedApplicationName() {
		return this.expectedApplicationName;
	}
}
