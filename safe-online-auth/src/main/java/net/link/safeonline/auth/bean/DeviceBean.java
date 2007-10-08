/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Device;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.DevicePolicyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("device")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "DeviceBean/local")
public class DeviceBean implements Device {

	private static final Log LOG = LogFactory.getLog(DeviceBean.class);

	private String selection;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private DevicePolicyService devicePolicyService;

	@In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
	private String application;

	@In(value = LoginManager.REQUIRED_DEVICES_ATTRIBUTE, required = false)
	private Set<AuthenticationDevice> requiredDevicePolicy;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.selection = null;
	}

	public String getSelection() {
		return this.selection;
	}

	public void setSelection(String deviceSelection) {
		this.selection = deviceSelection;
	}

	public String next() {
		LOG.debug("next: " + this.selection);
		if (null == this.selection) {
			LOG.debug("Please make a selection.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMakeSelection");
			return null;
		}
		return this.selection;
	}

	@Factory("applicationDevices")
	public List<SelectItem> applicationDevicesFactory() {
		LOG.debug("application devices factory");
		List<SelectItem> applicationDevices = new LinkedList<SelectItem>();
		try {
			Set<AuthenticationDevice> devicePolicy = this.devicePolicyService
					.getDevicePolicy(this.application,
							this.requiredDevicePolicy);
			for (AuthenticationDevice device : devicePolicy) {
				String deviceName = device.getDeviceName();
				SelectItem applicationDevice = new SelectItem(deviceName);
				applicationDevices.add(applicationDevice);
			}
		} catch (ApplicationNotFoundException e) {
			LOG.error("application not found: " + this.application);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
		} catch (EmptyDevicePolicyException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorEmptyDevicePolicy");
			LOG.error("empty device policy");
		}
		deviceNameDecoration(applicationDevices);
		return applicationDevices;
	}

	private static final Map<String, String> deviceNames = new HashMap<String, String>();

	static {
		deviceNames.put("password", "Username/password");
		deviceNames.put("beid", "Belgium Identity Card");
	}

	private void deviceNameDecoration(List<SelectItem> selectItems) {
		for (SelectItem selectItem : selectItems) {
			String deviceId = (String) selectItem.getValue();
			String deviceName = deviceNames.get(deviceId);
			if (null == deviceName) {
				deviceName = deviceId;
			}
			selectItem.setLabel(deviceName);
		}
	}
}
