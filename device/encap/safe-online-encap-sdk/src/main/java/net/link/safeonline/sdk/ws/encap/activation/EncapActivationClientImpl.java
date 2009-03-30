/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.encap.activation;

import http.MSecBankIdActivationSoapBindingStub;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.EncapConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import encap.msec.server.bus.ActivationInitResponse;
import encap.msec.server.bus.MSecResponse;


public class EncapActivationClientImpl implements EncapActivationClient {

    private final static Log                    LOG = LogFactory.getLog(EncapActivationClientImpl.class);

    private MSecBankIdActivationSoapBindingStub activationStub;


    public EncapActivationClientImpl(String location) throws AxisFault, MalformedURLException {

        URL endpointURL = new URL("http://" + location + "/services/mSecBankIdActivation");
        activationStub = new MSecBankIdActivationSoapBindingStub(endpointURL, new Service());
    }

    public String activate(String mobile, String orgId, String userId)
            throws RemoteException {

        LOG.debug("activate: " + mobile + ", " + orgId + ", " + userId);

        ActivationInitResponse response = activationStub.activate(mobile, orgId, userId);

        LOG.debug("activation result: " + response.getStatus());
        LOG.debug("activation info: " + response.getAdditionalInfo());
        LOG.debug("activation app id: " + response.getAppId());
        LOG.debug("activation client id: " + response.getClientId());
        LOG.debug("activation so: " + response.getSecureObject());
        LOG.debug("activation sp id: " + response.getServiceProviderId());
        LOG.debug("activation session id: " + response.getSessionId());
        LOG.debug("activation code: " + response.getActivationCode());
        if (EncapConstants.ENCAP_SUCCES == response.getStatus())
            return response.getActivationCode();
        return null;
    }

    public boolean cancelSession(String sessionId)
            throws RemoteException {

        LOG.debug("cancel session: " + sessionId);
        MSecResponse response = activationStub.cancelSession(sessionId);
        if (EncapConstants.ENCAP_SUCCES == response.getStatus())
            return true;
        return false;
    }
}
