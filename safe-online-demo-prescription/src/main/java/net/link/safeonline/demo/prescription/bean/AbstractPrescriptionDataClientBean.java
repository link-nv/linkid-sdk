/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

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

import net.link.safeonline.demo.prescription.AbstractPrescriptionDataClient;
import net.link.safeonline.demo.prescription.keystore.DemoPrescriptionKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


/**
 * Abstract class for data client beans. Inherit from this class if you need a {@link DataClient} component.
 *
 * @author fcorneli
 *
 */
public abstract class AbstractPrescriptionDataClientBean implements AbstractPrescriptionDataClient {

    @Logger
    private Log                                   log;

    @In(create = true)
    FacesMessages                                 facesMessages;

    private transient DataClient                  dataClient;

    private transient AttributeClient             attributeClient;

    private transient NameIdentifierMappingClient mappingClient;

    private String                                wsLocation;

    private X509Certificate                       certificate;

    private PrivateKey                            privateKey;


    @PostConstruct
    public void postConstructCallback() {

        this.log.debug("postConstruct");
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        this.wsLocation = externalContext.getInitParameter("WsLocation");
        PrivateKeyEntry privateKeyEntry = DemoPrescriptionKeyStoreUtils.getPrivateKeyEntry();
        this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
        this.privateKey = privateKeyEntry.getPrivateKey();
        postActivateCallback();
    }

    @PostActivate
    public void postActivateCallback() {

        this.log.debug("postActivate");
        this.dataClient = new DataClientImpl(this.wsLocation, this.certificate, this.privateKey);
        this.attributeClient = new AttributeClientImpl(this.wsLocation, this.certificate, this.privateKey);
        this.mappingClient = new NameIdentifierMappingClientImpl(this.wsLocation, this.certificate, this.privateKey);
    }

    @PrePassivate
    public void prePassivateCallback() {

        this.log.debug("prePassivate");
        this.dataClient = null;
        this.attributeClient = null;
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
        this.dataClient = null;
        this.attributeClient = null;
        this.wsLocation = null;
        this.certificate = null;
        this.privateKey = null;
    }

    protected DataClient getDataClient() {

        if (null == this.dataClient)
            throw new EJBException("data client not yet initialized");
        return this.dataClient;
    }

    protected AttributeClient getAttributeClient() {

        if (null == this.attributeClient)
            throw new EJBException("attribute client not yet initialized");
        return this.attributeClient;
    }

    protected NameIdentifierMappingClient getMappingClient() {

        if (null == this.mappingClient)
            throw new EJBException("mapping client not yet initialized");
        return this.mappingClient;
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
            username = tempAttributeClient.getAttributeValue(userId, DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME,
                    String.class);
        } catch (WSClientTransportException e) {
            this.facesMessages.add("connection error: " + e.getMessage());
            return null;
        } catch (RequestDeniedException e) {
            this.facesMessages.add("request denied");
            return null;
        } catch (AttributeNotFoundException e) {
            this.facesMessages.add("login attribute not found");
            return null;
        }

        this.log.debug("username = " + username);
        return username;
    }
}
