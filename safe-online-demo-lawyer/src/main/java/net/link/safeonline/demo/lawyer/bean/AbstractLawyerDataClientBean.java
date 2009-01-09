/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

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

import net.link.safeonline.demo.lawyer.AbstractLawyerDataClient;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.Attribute;
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
public abstract class AbstractLawyerDataClientBean implements AbstractLawyerDataClient {

    @Logger
    private Log                                   log;

    @In(create = true)
    FacesMessages                                 facesMessages;

    private transient DataClient                  dataClient;

    private transient AttributeClient             attributeClient;

    private transient NameIdentifierMappingClient identifierMappingClient;

    private String                                wsLocation;

    private X509Certificate                       certificate;

    private PrivateKey                            privateKey;


    @PostConstruct
    public void postConstructCallback() {

        log.debug("postConstruct");
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        wsLocation = externalContext.getInitParameter("WsLocation");
        PrivateKeyEntry privateKeyEntry = DemoLawyerKeyStoreUtils.getPrivateKeyEntry();
        certificate = (X509Certificate) privateKeyEntry.getCertificate();
        privateKey = privateKeyEntry.getPrivateKey();
        postActivateCallback();
    }

    @PostActivate
    public void postActivateCallback() {

        log.debug("postActivate: location=" + wsLocation);
        dataClient = new DataClientImpl(wsLocation, certificate, privateKey);
        attributeClient = new AttributeClientImpl(wsLocation, certificate, privateKey);
        identifierMappingClient = new NameIdentifierMappingClientImpl(wsLocation, certificate, privateKey);
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

    protected NameIdentifierMappingClient getNameIdentifierMappingClient() {

        if (null == identifierMappingClient)
            throw new EJBException("name identifier client not yet initialized");
        return identifierMappingClient;
    }

    /**
     * Gives back the lawyer status of a subject. This method also sets the {@link FacesMessages} in case something goes wrong.
     * 
     * @param userId
     * @return the lawyer status or <code>null</code> in case of error.
     */
    protected LawyerStatus getLawyerStatus(String userId) {

        boolean lawyer = false;
        boolean suspended = false;
        String bar = null;
        boolean barAdmin = false;
        Attribute<Boolean> lawyerAttribute;
        Attribute<Boolean> suspendedAttribute;
        Attribute<String> barAttribute;
        Attribute<Boolean> barAdminAttribute;
        DataClient currentDataClient = getDataClient();
        try {
            lawyerAttribute = currentDataClient.getAttributeValue(userId, DemoConstants.LAWYER_ATTRIBUTE_NAME, Boolean.class);
            suspendedAttribute = currentDataClient.getAttributeValue(userId, DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, Boolean.class);
            barAttribute = currentDataClient.getAttributeValue(userId, DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME, String.class);
            barAdminAttribute = currentDataClient.getAttributeValue(userId, DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, Boolean.class);
        } catch (WSClientTransportException e) {
            facesMessages.add("connection error: " + e.getMessage());
            return null;
        } catch (RequestDeniedException e) {
            facesMessages.add("request denied");
            return null;
        } catch (SubjectNotFoundException e) {
            facesMessages.add("subject not found");
            return null;
        }
        if (null != lawyerAttribute && null != lawyerAttribute.getValue()) {
            lawyer = lawyerAttribute.getValue();
        }
        if (null != suspendedAttribute && null != suspendedAttribute.getValue()) {
            suspended = suspendedAttribute.getValue();
        }
        if (null != barAttribute) {
            bar = barAttribute.getValue();
        }
        if (null != barAdminAttribute && null != barAdminAttribute.getValue()) {
            barAdmin = barAdminAttribute.getValue();
        }
        LawyerStatus lawyerStatus = new LawyerStatus(lawyer, suspended, bar, barAdmin);
        return lawyerStatus;
    }

    /**
     * Returns the username for this user Id. Sets {@link FacesMessages} in case something goes wrong.
     * 
     * @param userId
     */
    protected String getUsername(String userId) {

        String username = null;
        AttributeClient currentAttributeClient = getAttributeClient();
        try {
            username = currentAttributeClient.getAttributeValue(userId, DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
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
