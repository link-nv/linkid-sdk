/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import java.net.ConnectException;
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

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

import net.link.safeonline.demo.payment.AbstractPaymentDataClient;
import net.link.safeonline.demo.payment.CustomerStatus;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.data.Attribute;

/**
 * Abstract class for data client beans. Inherit from this class if you need a
 * {@link DataClient} component.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractPaymentDataClientBean implements
		AbstractPaymentDataClient {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	private transient DataClient dataClient;

	private String wsHostName;
	private String wsHostPort;

	private X509Certificate certificate;

	private PrivateKey privateKey;

	@PostConstruct
	public void postConstructCallback() {
		log.debug("postConstruct");
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		this.wsHostName = externalContext.getInitParameter("WsHostName");
		this.wsHostPort = externalContext.getInitParameter("WsHostPort");
		PrivateKeyEntry privateKeyEntry = DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
		this.privateKey = privateKeyEntry.getPrivateKey();
		postActivateCallback();
	}

	@PostActivate
	public void postActivateCallback() {
		log.debug("postActivate");
		this.dataClient = new DataClientImpl(this.wsHostName + ":"
				+ this.wsHostPort, this.certificate, this.privateKey);
	}

	@PrePassivate
	public void prePassivateCallback() {
		log.debug("prePassivate");
		this.dataClient = null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy");
		this.dataClient = null;
		this.wsHostName = null;
		this.wsHostPort = null;
		this.certificate = null;
		this.privateKey = null;
	}

	protected DataClient getDataClient() {
		if (null == this.dataClient) {
			throw new EJBException("data client not yet initialized");
		}
		return this.dataClient;
	}

	/**
	 * Gives back the lawyer status of a subject. This method also sets the
	 * {@link FacesMessages} in case something goes wrong.
	 * 
	 * @param subjectLogin
	 * @return the lawyer status or <code>null</code> in case of error.
	 */
	protected CustomerStatus getCustomerStatus(String subjectLogin) {
		boolean junior = false;
		boolean paymentAdmin = false;
		Attribute<Boolean> juniorAttribute;
		Attribute<Boolean> paymentAdminAttribute;
		DataClient dataClient = getDataClient();
		try {
			juniorAttribute = dataClient.getAttributeValue(subjectLogin,
					DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, Boolean.class);
			paymentAdminAttribute = dataClient.getAttributeValue(subjectLogin,
					DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME, Boolean.class);
		} catch (ConnectException e) {
			this.facesMessages.add("connection error: " + e.getMessage());
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.add("subject not found");
			return null;
		}
		if (null != juniorAttribute && null != juniorAttribute.getValue()) {
			junior = juniorAttribute.getValue();
		}
		if (null != paymentAdminAttribute
				&& null != paymentAdminAttribute.getValue()) {
			paymentAdmin = paymentAdminAttribute.getValue();
		}
		CustomerStatus customerStatus = new CustomerStatus(junior, paymentAdmin);
		return customerStatus;
	}
}
