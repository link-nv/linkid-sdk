/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.encap.authentication;

import java.net.URL;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.EncapConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import types._0._1.authentication.encap.safe_online.link.net.ChallengeResponse;
import types._0._1.authentication.encap.safe_online.link.net.VerifyResponse;
import types._0._1.encap.safe_online.link.net.MSecResponse;
import _0._1.authentication.encap.safe_online.link.net.MSecBankIdSoapBindingStub;

public class EncapAuthenticationClientImpl implements EncapAuthenticationClient {

	private final static Log LOG = LogFactory
			.getLog(EncapAuthenticationClientImpl.class);

	private MSecBankIdSoapBindingStub authStub;

	public EncapAuthenticationClientImpl(URL endpointURL) throws AxisFault {
		this.authStub = new MSecBankIdSoapBindingStub(endpointURL,
				new Service());
	}

	public boolean cancelSession(String sessionId) throws RemoteException {
		LOG.debug("cancel session: " + sessionId);
		MSecResponse response = this.authStub.cancelSession(sessionId);
		if (EncapConstants.ENCAP_SUCCES == response.getStatus())
			return true;
		return false;
	}

	public String challenge(String mobile, String orgId) throws RemoteException {
		LOG.debug("challenge mobile=" + mobile + " orgId=" + orgId);
		ChallengeResponse response = this.authStub.challenge(mobile, orgId);
		if (response.getStatus() == EncapConstants.ENCAP_FAILURE)
			return null;
		return response.getChallengeId();
	}

	public boolean verifyOTP(String challengeId, String OTPValue)
			throws RemoteException {
		LOG.debug("verify OTP: challengeId=" + challengeId + " OTPValue="
				+ OTPValue);
		VerifyResponse response = this.authStub
				.verifyOTP(challengeId, OTPValue);
		if (EncapConstants.ENCAP_SUCCES == response.getStatus())
			return true;
		if (EncapConstants.ENCAP_FAILURE_NO_INFO == response.getStatus())
			return false;
		if (EncapConstants.ENCAP_FAILURE == response.getStatus()) {
			LOG.debug("verifyOTP failed: " + response.getAdditionalInfo());
			return false;
		}
		return false;
	}

}
