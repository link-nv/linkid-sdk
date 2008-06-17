/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.backend.MobileManager;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.encap.Registration;
import net.link.safeonline.model.encap.EncapDeviceService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("registration")
@LocalBinding(jndiBinding = EncapConstants.JNDI_PREFIX
		+ "RegistrationBean/local")
public class RegistrationBean implements Registration {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@Out(required = false, scope = ScopeType.CONVERSATION)
	@In(required = false)
	private String mobile;

	@In
	private String userId;

	@In(value = ProtocolContext.PROTOCOL_CONTEXT)
	private ProtocolContext protocolContext;

	private String mobileActivationCode;

	@EJB
	private EncapDeviceService encapDeviceService;

	@EJB
	private MobileManager mobileManager;

	@EJB
	private SamlAuthorityService samlAuthorityService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
		this.mobile = null;
		this.mobileActivationCode = null;
	}

	public String mobileCancel() {
		this.protocolContext.setSuccess(false);
		exit();
		return null;
	}

	private void exit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		try {
			externalContext.redirect("./deviceexit");
		} catch (IOException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return;
		}
		this.protocolContext.setValidity(this.samlAuthorityService
				.getAuthnAssertionValidity());
	}

	@Begin
	public String mobileRegister() {
		this.log.debug("register mobile: " + this.mobile);
		try {
			this.mobileActivationCode = this.encapDeviceService.register(
					this.userId, this.mobile);
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (MobileRegistrationException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (ArgumentIntegrityException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMobileTaken");
			return null;
		}
		return "";
	}

	@End
	public String mobileActivationOk() {
		this.log.debug("mobile activation ok: " + this.mobile);
		this.mobileActivationCode = null;
		this.protocolContext.setSuccess(true);
		exit();
		return null;
	}

	@End
	public String mobileActivationCancel() {
		this.log.debug("mobile activation canceled: " + this.mobile);
		this.mobileActivationCode = null;
		try {
			this.encapDeviceService.remove(this.userId, this.mobile);
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		}
		this.protocolContext.setSuccess(false);
		exit();
		return null;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMobileActivationCode() {
		return this.mobileActivationCode;
	}

	public String getMobileClientLink() {
		return this.mobileManager.getClientDownloadLink();
	}

}
