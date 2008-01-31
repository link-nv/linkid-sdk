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
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.encap.MobileLogon;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("mobileLogon")
@Scope(ScopeType.SESSION)
@LocalBinding(jndiBinding = EncapConstants.JNDI_PREFIX
		+ "MobileLogonBean/local")
public class MobileLogonBean implements MobileLogon {

	private static final Log LOG = LogFactory.getLog(MobileLogonBean.class);

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private WeakMobileDeviceService weakMobileDeviceService;

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

	public String login() {
		LOG.debug("login: " + this.mobile);
		HelpdeskLogger.add("login: " + this.mobile, LogLevelType.INFO);
		try {
			SubjectEntity subject = this.weakMobileDeviceService.authenticate(
					this.mobile, this.challengeId, this.mobileOTP);
			if (null == subject) {
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				HelpdeskLogger.add("login failed: " + this.mobile,
						LogLevelType.ERROR);
				return null;
			}
			login(subject);
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: subject not found for " + this.mobile,
					LogLevelType.ERROR);
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
			HelpdeskLogger.add("login: encap webservice not available",
					LogLevelType.ERROR);
			return null;
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
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

	private void login(SubjectEntity subject) throws MobileException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpSession session = (HttpSession) externalContext.getSession(false);
		if (null == session)
			throw new MobileException("No HttpSession active");
		AuthenticationContext authenticationContext = AuthenticationContext
				.getLoginManager(session);
		authenticationContext.setUserId(subject.getUserId());
		authenticationContext.setValidity(this.samlAuthorityService
				.getAuthnAssertionValidity());
		authenticationContext.setIssuer(SafeOnlineConstants.ENCAP_DEVICE_ID);
		authenticationContext
				.setUsedDevice(SafeOnlineConstants.ENCAP_DEVICE_ID);

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

	public String requestOTP() {
		LOG.debug("request OTP: mobile=" + this.mobile);
		try {
			this.challengeId = this.weakMobileDeviceService
					.requestOTP(this.mobile);
			LOG.debug("received challengeId: " + this.challengeId);
		} catch (MalformedURLException e) {
			LOG.debug("requestOTP: MalformedURLException thrown: "
					+ e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MobileException e) {
			LOG.debug("requestOTP: MobileException thrown: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileAuthenticationFailed");
			return null;
		}
		return null;
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
	}

}
