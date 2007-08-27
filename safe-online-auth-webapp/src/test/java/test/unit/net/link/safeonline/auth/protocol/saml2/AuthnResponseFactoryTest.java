/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.protocol.saml2;

import static org.junit.Assert.assertNotNull;
import net.link.safeonline.auth.protocol.saml2.AuthnResponseFactory;
import net.link.safeonline.auth.protocol.saml2.SafeOnlineAuthnContextClass;
import net.link.safeonline.test.util.DomTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

public class AuthnResponseFactoryTest {

	private static final Log LOG = LogFactory
			.getLog(AuthnResponseFactoryTest.class);

	@Test
	public void createAuthnResponse() throws Exception {
		// setup
		String inResponseTo = "id-in-response-to-test-id";
		String issuerName = "test-issuer-name";
		String subjectName = "test-subject-name";

		// operate
		Response response = AuthnResponseFactory.createAuthResponse(
				inResponseTo, issuerName, subjectName,
				SafeOnlineAuthnContextClass.PASSWORD_PROTECTED_TRANSPORT);

		// verify
		assertNotNull(response);

		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(response);
		Element responseElement;
		try {
			responseElement = marshaller.marshall(response);
		} catch (MarshallingException e) {
			throw new RuntimeException("opensaml2 marshalling error: "
					+ e.getMessage(), e);
		}

		LOG.debug("response: " + DomTestUtils.domToString(responseElement));
	}
}
