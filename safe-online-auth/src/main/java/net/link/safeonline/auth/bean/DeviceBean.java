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
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Device;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.device.sdk.seam.SafeOnlineDeviceUtils;
import net.link.safeonline.entity.DeviceEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("device")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "DeviceBean/local")
public class DeviceBean implements Device {

	@In(create = true)
	FacesMessages facesMessages;

	@Logger
	private Log log;

	@EJB
	private DevicePolicyService devicePolicyService;

	@In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
	private String application;

	@In(value = LoginManager.REQUIRED_DEVICES_ATTRIBUTE, required = false)
	private Set<DeviceEntity> requiredDevicePolicy;

	@Out(required = false, scope = ScopeType.SESSION)
	private String deviceSelection;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	public String getSelection() {
		return this.deviceSelection;
	}

	public void setSelection(String deviceSelection) {
		this.deviceSelection = deviceSelection;
	}

	public String next() {
		this.log.debug("next: " + this.deviceSelection);
		if (null == this.deviceSelection) {
			this.log.debug("Please make a selection.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMakeSelection");
			return null;
		}
		String authenticationURL;
		try {
			authenticationURL = this.devicePolicyService
					.getAuthenticationURL(this.deviceSelection);
		} catch (DeviceNotFoundException e) {
			this.log.error("device not found: " + this.deviceSelection);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
		if (remoteURL(authenticationURL)) {
			return SafeOnlineDeviceUtils.authenticate(this.facesMessages, this.log,
					authenticationURL, this.deviceSelection);
		}
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect(authenticationURL);
		} catch (IOException e) {
			this.log.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
		}
		return null;
	}

	private boolean remoteURL(String authenticationURLName) {
		if (authenticationURLName.startsWith("http://")
				|| authenticationURLName.startsWith("https://"))
			return true;
		return false;
	}

	@Factory("applicationDevices")
	public List<SelectItem> applicationDevicesFactory() {
		this.log.debug("application devices factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		List<SelectItem> applicationDevices = new LinkedList<SelectItem>();
		try {
			List<DeviceEntity> devicePolicy = this.devicePolicyService
					.getDevicePolicy(this.application,
							this.requiredDevicePolicy);
			for (DeviceEntity device : devicePolicy) {
				String deviceName = this.devicePolicyService
						.getDeviceDescription(device.getName(), viewLocale);
				SelectItem applicationDevice = new SelectItem(device.getName(),
						deviceName);
				applicationDevices.add(applicationDevice);
			}
		} catch (ApplicationNotFoundException e) {
			this.log.error("application not found: " + this.application);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
		} catch (EmptyDevicePolicyException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorEmptyDevicePolicy");
			this.log.error("empty device policy");
		}
		return applicationDevices;
	}

	@Factory("allDevices")
	public List<SelectItem> allDevicesFactory() {
		this.log.debug("all devices factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		List<SelectItem> allDevices = new LinkedList<SelectItem>();

		List<DeviceEntity> devices = this.devicePolicyService.getDevices();

		for (DeviceEntity device : devices) {
			String deviceName = this.devicePolicyService.getDeviceDescription(
					device.getName(), viewLocale);
			SelectItem allDevice = new SelectItem(device.getName(), deviceName);
			allDevices.add(allDevice);
		}
		return allDevices;
	}
}
