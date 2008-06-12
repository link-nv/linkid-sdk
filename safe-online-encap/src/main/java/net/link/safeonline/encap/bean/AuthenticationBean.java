/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.encap.Authentication;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Name("authentication")
@LocalBinding(jndiBinding = EncapConstants.JNDI_PREFIX
		+ "AuthenticationBean/local")
public class AuthenticationBean implements Authentication {

	private static final Log LOG = LogFactory.getLog(AuthenticationBean.class);

	@In(create = true)
	FacesMessages facesMessages;

	@In(value = AuthenticationContext.AUTHENTICATION_CONTEXT)
	AuthenticationContext authenticationContext;

	@EJB
	private EncapDeviceService encapDeviceService;

	@EJB
	private SamlAuthorityService samlAuthorityService;

	private String challengeId;

	private String mobile;

	private String mobileOTP;

	public String getMobileOTP() {
		return this.mobileOTP;
	}

	public void setMobileOTP(String mobileOTP) {
		this.mobileOTP = mobileOTP;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getChallengeId() {
		return this.challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	@End
	public String login() {
		LOG.debug("login: " + this.mobile);
		HelpdeskLogger.add("login: " + this.mobile, LogLevelType.INFO);
		try {
			String deviceUserId = this.encapDeviceService.authenticate(
					this.mobile, this.challengeId, this.mobileOTP);
			if (null == deviceUserId) {
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				HelpdeskLogger.add("login failed: " + this.mobile,
						LogLevelType.ERROR);
				return null;
			}
			login(deviceUserId);
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
			HelpdeskLogger.add("login: subject not found for " + this.mobile,
					LogLevelType.ERROR);
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			HelpdeskLogger.add("login: encap webservice not available",
					LogLevelType.ERROR);
			return null;
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			HelpdeskLogger.add("login: failed to contact encap webservice for "
					+ this.mobile, LogLevelType.ERROR);
			return null;
		} catch (MobileAuthenticationException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileAuthenticationFailed");
			return null;
		}
		HelpdeskLogger.clear();
		destroyCallback();
		return null;
	}

	private void login(String deviceUserId) {
		this.authenticationContext.setUserId(deviceUserId);
		this.authenticationContext.setValidity(this.samlAuthorityService
				.getAuthnAssertionValidity());
		this.authenticationContext
				.setIssuer(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
		this.authenticationContext
				.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);

		exit();
	}

	@Begin
	public String requestOTP() {
		LOG.debug("request OTP: mobile=" + this.mobile);
		try {
			this.challengeId = this.encapDeviceService.requestOTP(this.mobile);
			LOG.debug("received challengeId: " + this.challengeId);
		} catch (MalformedURLException e) {
			LOG.debug("requestOTP: MalformedURLException thrown: "
					+ e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (MobileException e) {
			LOG.debug("requestOTP: MobileException thrown: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
			return null;
		} catch (SubjectNotFoundException e) {
			HelpdeskLogger.add("login: subject not found for " + this.mobile,
					LogLevelType.ERROR);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
			return null;
		}
		return "success";
	}

	public String cancel() {
		this.authenticationContext
				.setIssuer(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
		exit();
		return null;
	}

	private void exit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();

		String redirectUrl = "authenticationexit";
		LOG.debug("redirecting to: " + redirectUrl);
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			LOG.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return;
		}
	}

	@PostConstruct
	public void init() {
		HelpdeskLogger.clear();
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("remove");
		this.mobileOTP = null;
		this.mobile = null;
	}

}
