/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.EncapConstants;

import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import types._0._1.activation.encap.safe_online.link.net.ActivationInitResponse;
import types._0._1.administration.encap.safe_online.link.net.MSecResponse;
import types._0._1.authentication.encap.safe_online.link.net.ChallengeResponse;
import types._0._1.authentication.encap.safe_online.link.net.VerifyResponse;
import _0._1.activation.encap.safe_online.link.net.MSecBankIdActivationSoapBindingStub;
import _0._1.administration.encap.safe_online.link.net.MSecBankIdAdministrationSoapBindingStub;
import _0._1.authentication.encap.safe_online.link.net.MSecBankIdSoapBindingStub;

public class EncapWebServiceTest {

	private static final String VALID_MOBILE = "0900444555";

	private static final String INVALID_MOBILE = "012345678910";

	private static final Log LOG = LogFactory.getLog(EncapWebServiceTest.class);

	@Test
	public void testEncapActivate() throws MalformedURLException,
			RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankIdActivation";
		URL endpointURL = new URL(endpoint);
		MSecBankIdActivationSoapBindingStub activationStub = new MSecBankIdActivationSoapBindingStub(
				endpointURL, new Service());

		// operate
		ActivationInitResponse activationResponse = activationStub.activate(
				VALID_MOBILE, "1", null);
		LOG.debug("response: " + activationResponse.getStatus());
	}

	@Test
	public void testEncapLock() throws MalformedURLException, RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankIdAdministration";
		URL endpointURL = new URL(endpoint);
		MSecBankIdAdministrationSoapBindingStub adminStub = new MSecBankIdAdministrationSoapBindingStub(
				endpointURL, new Service());

		// operate
		MSecResponse adminResponse = adminStub.lock(VALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_SUCCES, adminResponse.getStatus());

		// operate
		adminResponse = adminStub.lock(INVALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_FAILURE, adminResponse.getStatus());
	}

	@Test
	public void testEncapUnLock() throws MalformedURLException, RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankIdAdministration";
		URL endpointURL = new URL(endpoint);
		MSecBankIdAdministrationSoapBindingStub adminStub = new MSecBankIdAdministrationSoapBindingStub(
				endpointURL, new Service());

		// operate
		MSecResponse adminResponse = adminStub.unLock(VALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_SUCCES, adminResponse.getStatus());

		// operate
		adminResponse = adminStub.unLock(INVALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_FAILURE, adminResponse.getStatus());
	}

	@Test
	public void testEncapRemove() throws MalformedURLException, RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankIdAdministration";
		URL endpointURL = new URL(endpoint);
		MSecBankIdAdministrationSoapBindingStub adminStub = new MSecBankIdAdministrationSoapBindingStub(
				endpointURL, new Service());

		// operate
		MSecResponse adminResponse = adminStub.remove(VALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_SUCCES, adminResponse.getStatus());

		// operate
		adminResponse = adminStub.remove(INVALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_FAILURE, adminResponse.getStatus());
	}

	@Test
	public void testEncapShowStatus() throws MalformedURLException,
			RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankIdAdministration";
		URL endpointURL = new URL(endpoint);
		MSecBankIdAdministrationSoapBindingStub adminStub = new MSecBankIdAdministrationSoapBindingStub(
				endpointURL, new Service());

		// operate
		MSecResponse adminResponse = adminStub.showStatus(VALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_SUCCES, adminResponse.getStatus());

		// operate
		adminResponse = adminStub.showStatus(INVALID_MOBILE, "-1");
		// verify
		assertEquals(EncapConstants.ENCAP_FAILURE, adminResponse.getStatus());
	}

	@Test
	public void testEncapChallenge() throws MalformedURLException,
			RemoteException {
		// setup
		String endpoint = "http://localhost:8080/safe-online-encap-ws/services/mSecBankId";
		URL endpointURL = new URL(endpoint);
		MSecBankIdSoapBindingStub authStub = new MSecBankIdSoapBindingStub(
				endpointURL, new Service());

		// operate
		ChallengeResponse challengeResponse = authStub.challenge(VALID_MOBILE,
				"-1");
		// verify
		assertEquals(EncapConstants.ENCAP_SUCCES, challengeResponse.getStatus());
		// operate
		VerifyResponse verifyResponse = authStub.verifyOTP(challengeResponse
				.getChallengeId(), "000000");
		// verify
		assertNotNull(verifyResponse);
	}
}
