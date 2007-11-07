/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

/**
 * MSecBankIdActivationSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package net.link.safeonline.encap.administration.ws;

import http.BankIdAdministration;
import http.MSecResponse;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EncapAdministrationSoapBindingImpl implements BankIdAdministration {

	private final static Log LOG = LogFactory
			.getLog(EncapAdministrationSoapBindingImpl.class);

	private final static int ENCAP_SUCCES = 0;

	private final static int ENCAP_FAILURE = 3;

	/**
	 * @param msisdn
	 *            Mobile telephone number
	 * @param orgId
	 *            Bank organisation identifier
	 * @return MSecResponse
	 * 
	 */
	public MSecResponse lock(String msisdn, String orgId)
			throws RemoteException {
		LOG.debug("lock: msisdn=" + msisdn + " orgId=" + orgId);
		MSecResponse response = new MSecResponse();
		if (checkMobile(msisdn)) {
			response.setStatus(ENCAP_SUCCES);
			response.setAdditionalInfo("Status mobile " + msisdn);
		} else {
			response.setStatus(ENCAP_FAILURE);
			response.setAdditionalInfo("Failed to lock mobile " + msisdn);
		}
		return response;

	}

	public MSecResponse remove(String msisdn, String orgId)
			throws RemoteException {
		LOG.debug("remove: msisdn=" + msisdn + " orgId=" + orgId);
		MSecResponse response = new MSecResponse();
		if (checkMobile(msisdn)) {
			response.setStatus(ENCAP_SUCCES);
			response.setAdditionalInfo("Status mobile " + msisdn);
		} else {
			response.setStatus(ENCAP_FAILURE);
			response.setAdditionalInfo("Failed to remove mobile " + msisdn);
		}
		return response;
	}

	public MSecResponse showStatus(String msisdn, String orgId)
			throws RemoteException {
		LOG.debug("showStatus: msisdn=" + msisdn + " orgId=" + orgId);
		MSecResponse response = new MSecResponse();
		if (checkMobile(msisdn)) {
			response.setStatus(ENCAP_SUCCES);
			response.setAdditionalInfo("Status mobile " + msisdn);
		} else {
			response.setStatus(ENCAP_FAILURE);
			response
					.setAdditionalInfo("Failed to show status mobile " + msisdn);
		}
		return response;

	}

	public MSecResponse unLock(String msisdn, String orgId)
			throws RemoteException {
		LOG.debug("unLock: msisdn=" + msisdn + " orgId=" + orgId);
		MSecResponse response = new MSecResponse();
		if (checkMobile(msisdn)) {
			response.setStatus(ENCAP_SUCCES);
			response.setAdditionalInfo("Status mobile " + msisdn);
		} else {
			response.setStatus(ENCAP_FAILURE);
			response.setAdditionalInfo("Failed to unlock mobile " + msisdn);
		}
		return response;
	}

	private boolean checkMobile(String mobile) {
		if (mobile.length() > 10) {
			LOG.error("Invalid mobile number: " + mobile);
			return false;
		}
		return true;
	}
}
