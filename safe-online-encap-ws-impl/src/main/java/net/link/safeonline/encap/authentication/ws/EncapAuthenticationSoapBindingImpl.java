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

package net.link.safeonline.encap.authentication.ws;

import http.BankIdAuthentication;

import java.rmi.RemoteException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import encap.msec.bankid.domain.ChallengeResponse;
import encap.msec.bankid.domain.VerifyResponse;
import encap.msec.server.bus.MSecResponse;


public class EncapAuthenticationSoapBindingImpl implements BankIdAuthentication {

    private final static Log LOG                   = LogFactory.getLog(EncapAuthenticationSoapBindingImpl.class);

    private final static int ENCAP_SUCCES          = 0;

    private final static int ENCAP_FAILURE_NO_INFO = 1;

    private final static int ENCAP_FAILURE         = 3;


    public MSecResponse cancelSession(String sessionId) throws RemoteException {

        LOG.debug("session canceled: " + sessionId);
        MSecResponse response = new MSecResponse();
        response.setSessionId(sessionId);
        response.setStatus(ENCAP_SUCCES);
        return response;
    }

    public ChallengeResponse challenge(String msisdn, String orgId) throws RemoteException {

        LOG.debug("challenge: msisdn=" + msisdn + " orgId=" + orgId);
        ChallengeResponse response = new ChallengeResponse();
        Random generator = new Random();
        Long challengeId = Math.abs(generator.nextLong()) % 999999999999L;
        LOG.debug("generated challenge id: " + challengeId);
        response.setChallengeId(challengeId.toString());
        response.setStatus(ENCAP_SUCCES);
        return response;
    }

    public VerifyResponse verifyOTP(String challengeId, String OTPValue) throws RemoteException {

        LOG.debug("verifyOTP: challengeId=" + challengeId + " OTPValue=" + OTPValue);
        VerifyResponse response = new VerifyResponse();
        if (challengeId.length() > 12) {
            LOG.debug("invalid challenge ID: " + challengeId);
            response.setStatus(ENCAP_FAILURE);
            response.setAdditionalInfo("Invalid challenge ID");
            return response;
        }
        if (OTPValue.length() > 6) {
            LOG.debug("invalid OTPValue: " + OTPValue);
            response.setStatus(ENCAP_FAILURE);
            response.setAdditionalInfo("Invalid OTP: too long");
            return response;
        }
        int otp = Integer.parseInt(OTPValue);
        if (otp % 2 == 0) {
            LOG.debug("Valid OTPValue received");
            response.setStatus(ENCAP_SUCCES);
        } else {
            LOG.debug("Invalid OTPValue received");
            response.setStatus(ENCAP_FAILURE_NO_INFO);
        }
        return response;
    }
}
