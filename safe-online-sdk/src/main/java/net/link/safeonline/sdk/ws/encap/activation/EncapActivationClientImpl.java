/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.encap.activation;

import java.net.URL;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.EncapConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import types._0._1.encap.safe_online.link.net.ActivationInitResponse;
import types._0._1.encap.safe_online.link.net.MSecResponse;
import _0._1.activation.encap.safe_online.link.net.MSecBankIdActivationSoapBindingStub;

public class EncapActivationClientImpl implements EncapActivationClient {

	private final static Log LOG = LogFactory
			.getLog(EncapActivationClientImpl.class);

	private MSecBankIdActivationSoapBindingStub activationStub;

	public EncapActivationClientImpl(URL endpointURL) throws AxisFault {
		this.activationStub = new MSecBankIdActivationSoapBindingStub(
				endpointURL, new Service());
	}

	public boolean activate(String mobile, String orgId, String userId)
			throws RemoteException {
		LOG.debug("activate: " + mobile + ", " + orgId + ", " + userId);

		ActivationInitResponse response = this.activationStub.activate(mobile,
				orgId, userId);
		LOG.debug("activation result: " + response.getStatus());
		if (EncapConstants.ENCAP_SUCCES == response.getStatus())
			return true;
		return false;
	}

	public boolean cancelSession(String sessionId) throws RemoteException {
		LOG.debug("cancel session: " + sessionId);
		MSecResponse response = this.activationStub.cancelSession(sessionId);
		if (EncapConstants.ENCAP_SUCCES == response.getStatus())
			return true;
		return false;
	}
}
