/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.DeviceRegistration;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.RegisteredDeviceService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;

@Stateful
@Name("deviceRegistration")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "DeviceRegistrationBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
public class DeviceRegistrationBean extends AbstractLoginBean implements
		DeviceRegistration {

	@Logger
	private Log log;

	private String device;

	private String password;

	@Out(required = false, scope = ScopeType.CONVERSATION)
	@In(required = false)
	private String mobile;

	private String mobileActivationCode;

	@In
	private AuthenticationService authenticationService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@EJB
	private DeviceService deviceService;

	@EJB
	private RegisteredDeviceService registeredDeviceService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
		this.device = null;
		this.password = null;
		this.mobile = null;
		this.mobileActivationCode = null;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String deviceNext() {
		this.log.debug("deviceNext: " + this.device);

		String registrationURL;
		try {
			/*
			 * TODO: Send the Id of this registered device to the device issuer
			 * in a SAML request.
			 */
			SubjectEntity subjectEntity = this.subjectService
					.findSubjectFromUserName(this.username);
			DeviceEntity deviceEntity = this.deviceService
					.getDevice(this.device);
			RegisteredDeviceEntity registeredDevice = this.registeredDeviceService
					.getDeviceRegistration(subjectEntity, deviceEntity);

			registrationURL = this.devicePolicyService
					.getRegistrationURL(this.device);
		} catch (DeviceNotFoundException e) {
			this.log.error("device not found: " + this.device);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
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

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getDevice() {
		return this.device;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public void setDevice(String device) {
		this.device = device;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getPassword() {
		return this.password;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String passwordNext() {
		this.log.debug("passwordNext");
		try {
			this.authenticationService.setPassword(this.password);
		} catch (PermissionDeniedException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		super.relogin(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		return null;
	}

	@Begin
	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String mobileRegister() {
		this.log.debug("register mobile: " + this.mobile);
		try {
			this.mobileActivationCode = this.authenticationService
					.registerMobile(this.mobile);
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
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
	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String mobileActivationOk() {
		this.log.debug("mobile activation ok: " + this.mobile);
		this.mobileActivationCode = null;
		return "";
	}

	@End
	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String mobileActivationCancel() {
		this.log.debug("mobile activation canceled: " + this.mobile);
		this.mobileActivationCode = null;
		try {
			this.authenticationService.removeMobile(this.mobile);
		} catch (MobileException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		} catch (MalformedURLException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "mobileRegistrationFailed");
			return null;
		}
		return "";
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public void setPassword(String password) {
		this.password = password;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getUsername() {
		return this.subjectService.getSubjectLogin(this.username);
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getMobile() {
		return this.mobile;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String getMobileActivationCode() {
		return this.mobileActivationCode;
	}

	@Factory("allDevices")
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
