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
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.demo.lawyer.LawyerStatusManager;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.data.DataValue;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("lawyerStatusManager")
@LocalBinding(jndiBinding = "SafeOnlineLawyerDemo/LawyerStatusManagerBean/local")
@SecurityDomain("demo-lawyer")
public class LawyerStatusManagerBean implements LawyerStatusManager {

	private transient DataClient dataClient;

	private String location;

	private X509Certificate certificate;

	private PrivateKey privateKey;

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@Resource
	private SessionContext sessionContext;

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy");
	}

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

	@Factory("lawyerStatus")
	@RolesAllowed(LawyerConstants.USER_ROLE)
	public LawyerStatus lawyerStatusFactory() {
		log.debug("lawyerStatusFactory");
		boolean lawyer = false;
		boolean suspended = false;
		String bar = null;
		String subjectLogin = this.sessionContext.getCallerPrincipal()
				.getName();
		DataValue lawyerAttribute;
		DataValue suspendedAttribute;
		DataValue barAttribute;
		try {
			lawyerAttribute = this.dataClient.getAttributeValue(subjectLogin,
					DemoConstants.LAWYER_ATTRIBUTE_NAME);
			suspendedAttribute = this.dataClient
					.getAttributeValue(subjectLogin,
							DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME);
			barAttribute = this.dataClient.getAttributeValue(subjectLogin,
					DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME);
		} catch (ConnectException e) {
			this.facesMessages.add("connection error: " + e.getMessage());
			return new LawyerStatus();
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return new LawyerStatus();
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
