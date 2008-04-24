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

package net.link.safeonline.encap.activation.ws;

import http.BankIdActivation;

import java.rmi.RemoteException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import encap.msec.server.bus.ActivationInitResponse;
import encap.msec.server.bus.MSecResponse;

public class EncapActivationSoapBindingImpl implements BankIdActivation {

	private final static Log LOG = LogFactory
			.getLog(EncapActivationSoapBindingImpl.class);

	private final static int ENCAP_SUCCES = 0;

	private final static int ENCAP_FAILURE = 3;

	public ActivationInitResponse activate(String msisdn, String orgId,
			Object secureObject) throws RemoteException {
		LOG.debug("activate: msisdn=" + msisdn + " orgId=" + orgId);
		ActivationInitResponse response = new ActivationInitResponse();
		if ((System.currentTimeMillis() % 2) == 0) {
			response.setStatus(ENCAP_SUCCES);
			Random generator = new Random();
			Long activationCode = Math.abs(generator.nextLong()) % 999999L;
			response.setSessionId(activationCode.toString());
			response.setActivationCode(activationCode.toString());
		} else {
			response.setStatus(ENCAP_FAILURE);
			response.setAdditionalInfo("Failed to activate mobile: " + msisdn);
		}
		return response;
	}

	public MSecResponse cancelSession(String sessionId) throws RemoteException {
		LOG.debug("session canceled: " + sessionId);
		MSecResponse response = new MSecResponse();
		response.setSessionId(sessionId);
		response.setStatus(ENCAP_SUCCES);
		return response;
	}
}
