/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.spike.net.link.safeonline.saml;

import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthenticationQuery;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLRequest;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;

public class SamlTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(SamlTest.class);

	public void testAuthenticationRequest() throws Exception {
		SAMLNameIdentifier id = new SAMLNameIdentifier();
		id.setName("test-username");
		id.setNameQualifier("test-password");
		id.setFormat(SAMLNameIdentifier.FORMAT_UNSPECIFIED);
		SAMLSubject subject = new SAMLSubject();
		subject.setNameIdentifier(id);
		SAMLAuthenticationQuery query = new SAMLAuthenticationQuery(subject,
				SAMLAuthenticationStatement.AuthenticationMethod_Password);

		SAMLRequest authRequest = new SAMLRequest(query);
		String result = authRequest.toString();
		LOG.debug("authentication request: " + result);
	}
	
	public void testAuthenticationResponse() throws Exception {
		SAMLNameIdentifier nameId = new SAMLNameIdentifier();
		nameId.setName("test-name");
		nameId.setFormat(SAMLNameIdentifier.FORMAT_UNSPECIFIED);
		SAMLSubject subject = new SAMLSubject();
		subject.setNameIdentifier(nameId);
		
		SAMLAuthenticationStatement authStatement = new SAMLAuthenticationStatement();
		authStatement.setAuthMethod(SAMLAuthenticationStatement.AuthenticationMethod_Password);
		authStatement.setSubject(subject);
		authStatement.setAuthInstant(new Date());
		
		SAMLAssertion assertion = new SAMLAssertion();
		assertion.setIssuer("test-asserting-party");
		assertion.addStatement(authStatement);
		
		SAMLResponse response = new SAMLResponse();
		response.setId(UUID.randomUUID().toString());
		response.addAssertion(assertion);
		String result = response.toString();
		LOG.debug("authentication response: " + result);
	}
}
