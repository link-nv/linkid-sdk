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
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.DeviceRegistration;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.entity.DeviceEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
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

	@In(required = true)
	private AuthenticationService authenticationService;

	@In(value = LoginManager.USERID_ATTRIBUTE, required = true)
	private String userId;

	@EJB
	private DevicePolicyService devicePolicyService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
		this.device = null;
		this.password = null;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
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
		if (this.device.equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
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
		AuthenticationUtils.redirect(this.facesMessages, registrationURL,
				this.device, this.userId);
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
			this.authenticationService
					.setPassword(this.username, this.password);
			this.authenticationService.authenticate(this.username,
					this.password);
		} catch (DeviceNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		}

		super.relogin(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);
		return null;
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
	@Factory("allDevicesDeviceRegistration")
	public List<SelectItem> allDevicesFactory() {
		this.log.debug("all devices factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		List<SelectItem> allDevices = new LinkedList<SelectItem>();

		List<DeviceEntity> devices = this.devicePolicyService.getDevices();

		for (DeviceEntity deviceEntity : devices) {
			if (!deviceEntity.isRegistrable()) {
				continue;
			}
			String deviceName = this.devicePolicyService.getDeviceDescription(
					deviceEntity.getName(), viewLocale);
			SelectItem allDevice = new SelectItem(deviceEntity.getName(),
					deviceName);
			allDevices.add(allDevice);
		}
		return allDevices;
	}

}
