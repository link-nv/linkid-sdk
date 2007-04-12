/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

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

import net.link.safeonline.demo.lawyer.AbstractLawyerDataClient;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.data.DataValue;

/**
 * Abstract class for data client beans. Inherit from this class if you need a
 * {@link DataClient} component.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractLawyerDataClientBean implements
		AbstractLawyerDataClient {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	private transient DataClient dataClient;

	private String location;

	private X509Certificate certificate;

	private PrivateKey privateKey;

	@PostConstruct
	public void postConstructCallback() {
		log.debug("postConstruct");
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		this.location = externalContext.getInitParameter("LocalHostName");
		PrivateKeyEntry privateKeyEntry = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
		this.privateKey = privateKeyEntry.getPrivateKey();
		postActivateCallback();
	}

	@PostActivate
	public void postActivateCallback() {
		log.debug("postActivate");
		this.dataClient = new DataClientImpl(this.location, this.certificate,
				this.privateKey);
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
		this.location = null;
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
	protected LawyerStatus getLawyerStatus(String subjectLogin) {
		boolean lawyer = false;
		boolean suspended = false;
		String bar = null;
		DataValue lawyerAttribute;
		DataValue suspendedAttribute;
		DataValue barAttribute;
		DataClient dataClient = getDataClient();
		try {
			lawyerAttribute = dataClient.getAttributeValue(subjectLogin,
					DemoConstants.LAWYER_ATTRIBUTE_NAME);
			suspendedAttribute = dataClient.getAttributeValue(subjectLogin,
					DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME);
			barAttribute = dataClient.getAttributeValue(subjectLogin,
					DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME);
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
		if (null != lawyerAttribute
				&& "true".equals(lawyerAttribute.getValue())) {
			lawyer = true;
		}
		if (null != suspendedAttribute
				&& "true".equals(suspendedAttribute.getValue())) {
			suspended = true;
		}
		if (null != barAttribute) {
			bar = barAttribute.getValue();
		}
		LawyerStatus lawyerStatus = new LawyerStatus(lawyer, suspended, bar);
		return lawyerStatus;
	}
}
