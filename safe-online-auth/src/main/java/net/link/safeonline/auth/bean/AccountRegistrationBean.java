/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import net.link.safeonline.auth.AccountRegistration;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

@Stateful
@Name("accountRegistration")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "AccountRegistrationBean/local")
public class AccountRegistrationBean extends AbstractLoginBean implements
		AccountRegistration {

	@EJB
	private UserRegistrationService userRegistrationService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@EJB
	private NodeAuthenticationService nodeAuthenticationService;

	@Logger
	private Log log;

	private String login;

	private String device;

	private String captcha;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String loginNext() {
		this.log.debug("loginNext");

		this.log.debug("captcha: " + this.captcha);

		if (null == this.captchaService) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNoCaptcha");
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		HttpSession httpSession = (HttpSession) externalContext
				.getSession(false);
		String captchaId = httpSession.getId();
		this.log.debug("captcha Id: " + captchaId);

		boolean valid;
		try {
			valid = this.captchaService.validateResponseForID(captchaId,
					this.captcha);
		} catch (CaptchaServiceException e) {
			/*
			 * It's possible that a data race occurs between the Captcha servlet
			 * and this validation call. In that case we just ask the user to
			 * try again.
			 */
			this.facesMessages.addToControlFromResourceBundle("captcha",
					FacesMessage.SEVERITY_ERROR, "errorNoCaptchaValidation");
			return null;
		}
		if (false == valid) {
			this.facesMessages.addToControlFromResourceBundle("captcha",
					FacesMessage.SEVERITY_ERROR, "errorInvalidCaptcha");
			return null;
		}

		SubjectEntity subject;
		try {
			subject = this.userRegistrationService.registerUser(this.login);
		} catch (ExistingUserException e) {
			this.facesMessages.addToControlFromResourceBundle("login",
					FacesMessage.SEVERITY_ERROR, "errorLoginTaken");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			this.facesMessages.addToControlFromResourceBundle("login",
					FacesMessage.SEVERITY_ERROR, "errorLoginTaken");
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages.addToControlFromResourceBundle("login",
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}

		this.username = subject.getUserId();
		return "next";
	}

	public String deviceNext() {
		this.log.debug("deviceNext: " + this.device);

		String registrationURL;
		try {
			registrationURL = this.devicePolicyService
					.getRegistrationURL(this.device);
		} catch (DeviceNotFoundException e) {
			this.log.error("device not found: " + this.device);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
		try {
			registrationURL += "?source=auth&node=" + getNodeName();
		} catch (NodeNotFoundException e) {
			this.log.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect(registrationURL);
		} catch (IOException e) {
			this.log.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
		}
		return null;
	}

	private String getNodeName() throws NodeNotFoundException {
		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		return this.nodeAuthenticationService
				.authenticate(authIdentityServiceClient.getCertificate());
	}

	public String getDevice() {
		return this.device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	@In(required = false, value = "CaptchaService", scope = ScopeType.SESSION)
	ImageCaptchaService captchaService;

	public String getCaptcha() {
		return this.captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	@Factory("allDevicesReg")
	public List<SelectItem> allDevicesFactory() {
		this.log.debug("all devices factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		List<SelectItem> allDevices = new LinkedList<SelectItem>();

		List<DeviceEntity> devices = this.devicePolicyService.getDevices();

		for (DeviceEntity deviceEntity : devices) {
			String deviceName = this.devicePolicyService.getDeviceDescription(
					deviceEntity.getName(), viewLocale);
			SelectItem allDevice = new SelectItem(deviceEntity.getName(),
					deviceName);
			allDevices.add(allDevice);
		}
		return allDevices;
	}
}
