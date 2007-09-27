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
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

/**
 * Abstract class for data client beans. Inherit from this class if you need a
 * {@link DataClient} component.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractPrescriptionDataClientBean implements
		AbstractPrescriptionDataClient {

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
		PrivateKeyEntry privateKeyEntry = DemoPrescriptionKeyStoreUtils
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
}
