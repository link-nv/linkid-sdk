/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.encap.administration;

import http.MSecBankIdAdministrationSoapBindingStub;
import http.MSecResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import net.link.safeonline.sdk.ws.encap.EncapConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EncapAdministrationClientImpl implements EncapAdministrationClient {

    private final static Log                        LOG = LogFactory.getLog(EncapAdministrationClientImpl.class);

    private MSecBankIdAdministrationSoapBindingStub adminStub;


    public EncapAdministrationClientImpl(String location) throws AxisFault, MalformedURLException {

        URL endpointURL = new URL("http://" + location + "/services/mSecBankIdAdministration");
        this.adminStub = new MSecBankIdAdministrationSoapBindingStub(endpointURL, new Service());
    }

    public boolean lock(String mobile, String orgId) throws RemoteException {

        LOG.debug("lock mobile: " + mobile);
        MSecResponse response = this.adminStub.lock(mobile, orgId);
        if (EncapConstants.ENCAP_SUCCES == response.getStatus())
            return true;
        return false;
    }

    public boolean remove(String mobile, String orgId) throws RemoteException {

        LOG.debug("remove mobile: " + mobile);
        MSecResponse response = this.adminStub.remove(mobile, orgId);
        if (EncapConstants.ENCAP_SUCCES == response.getStatus())
            return true;
        return false;
    }

    public String showStatus(String mobile, String orgId) throws RemoteException {

        LOG.debug("show status mobile: " + mobile);
        MSecResponse response = this.adminStub.showStatus(mobile, orgId);
        return response.getAdditionalInfo();
    }

    public boolean unLock(String mobile, String orgId) throws RemoteException {

        LOG.debug("unLock mobile: " + mobile);
        MSecResponse response = this.adminStub.unLock(mobile, orgId);
        if (EncapConstants.ENCAP_SUCCES == response.getStatus())
            return true;
        return false;
    }

}
