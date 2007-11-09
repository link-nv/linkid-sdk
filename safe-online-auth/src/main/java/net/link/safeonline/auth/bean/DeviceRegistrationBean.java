/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.DeviceRegistration;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.ResourceBundle;
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

	private String mobile;

	@In
	private AuthenticationService authenticationService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String deviceNext() {
		this.log.debug("deviceNext: " + this.device);
		return this.device;
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
		super.relogin(AuthenticationDevice.PASSWORD);
		return null;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String mobileNext() {
		this.log.debug("mobileNext");
		try {
			this.authenticationService.registerMobile(this.mobile);
		} catch (RemoteException e) {
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
		super.relogin(AuthenticationDevice.WEAK_MOBILE);
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
	public String getMobile() {
		return this.mobile;
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Factory("allDevices")
	public List<SelectItem> allDevicesFactory() {
		this.log.debug("all devices factory");
		List<SelectItem> allDevices = new LinkedList<SelectItem>();
		Set<AuthenticationDevice> devices = this.devicePolicyService
				.getDevices();
		for (AuthenticationDevice authDevice : devices) {
			String deviceName = authDevice.getDeviceName();
			SelectItem allDevice = new SelectItem(deviceName);
			allDevices.add(allDevice);
		}
		deviceNameDecoration(allDevices);
		return allDevices;
	}

	private void deviceNameDecoration(List<SelectItem> selectItems) {
		for (SelectItem selectItem : selectItems) {
			String deviceId = (String) selectItem.getValue();
			try {
				java.util.ResourceBundle bundle = ResourceBundle.instance();
				String deviceName = bundle.getString(deviceId);
				if (null == deviceName) {
					deviceName = deviceId;
				}
				selectItem.setLabel(deviceName);

			} catch (MissingResourceException e) {
				this.log.debug("resource not found: " + deviceId);
			}
		}
	}
}
