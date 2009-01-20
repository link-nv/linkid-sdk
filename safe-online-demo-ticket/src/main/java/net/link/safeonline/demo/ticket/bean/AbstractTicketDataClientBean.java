/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.demo.ticket.AbstractTicketDataClient;
import net.link.safeonline.demo.ticket.keystore.DemoTicketKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


/**
 * Abstract class for data client beans. Inherit from this class if you need a {@link DataClient} component.
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractTicketDataClientBean implements AbstractTicketDataClient {

    @Logger
    private Log                       log;

    @In(create = true)
    FacesMessages                     facesMessages;

    private transient DataClient      dataClient;

    private transient AttributeClient attributeClient;

    protected String                  wsLocation;

    protected String                  demoHostName;
    protected String                  demoHostPort;

    private X509Certificate           certificate;

    private PrivateKey                privateKey;


    @PostConstruct
    public void postConstructCallback() {

        log.debug("postConstruct");
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        demoHostName = externalContext.getInitParameter("DemoHostName");
        demoHostPort = externalContext.getInitParameter("DemoHostPort");
        wsLocation = externalContext.getInitParameter("WsLocation");
        PrivateKeyEntry privateKeyEntry = DemoTicketKeyStoreUtils.getPrivateKeyEntry();
        certificate = (X509Certificate) privateKeyEntry.getCertificate();
        privateKey = privateKeyEntry.getPrivateKey();
        postActivateCallback();
    }

    @PostActivate
    public void postActivateCallback() {

        log.debug("postActivate");
        dataClient = new DataClientImpl(wsLocation, certificate, privateKey);
        attributeClient = new AttributeClientImpl(wsLocation, certificate, privateKey);
    }

    @PrePassivate
    public void prePassivateCallback() {

        log.debug("prePassivate");
        dataClient = null;
        attributeClient = null;
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        log.debug("destroy");
        dataClient = null;
        attributeClient = null;
        wsLocation = null;
        certificate = null;
        privateKey = null;
    }

    protected DataClient getDataClient() {

        if (null == dataClient)
            throw new EJBException("data client not yet initialized");
        return dataClient;
    }

    protected AttributeClient getAttributeClient() {

        if (null == attributeClient)
            throw new EJBException("attribute client not yet initialized");
        return attributeClient;
    }

    /**
     * Returns the username for this user Id. Sets {@link FacesMessages} in case something goes wrong.
     * 
     * @param userId
     */
    protected String getUsername(String userId) {

        String username = null;
        AttributeClient tempAttributeClient = getAttributeClient();
        try {
            username = tempAttributeClient.getAttributeValue(userId, DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (AttributeNotFoundException e) {
            facesMessages.add("login attribute not found");
            return null;
        } catch (AttributeUnavailableException e) {
            facesMessages.add("login attribute unavailable");
            return null;
        }

        log.debug("username = " + username);
        return username;
    }
}
