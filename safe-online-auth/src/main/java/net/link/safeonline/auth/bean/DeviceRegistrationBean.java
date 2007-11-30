/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.net.MalformedURLException;
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
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.DevicePolicyService;

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

	@Out(required = false, scope = ScopeType.CONVERSATION)
	@In(required = false)
	private String mobile;

	private String mobileActivationCode;

	@In
	private AuthenticationService authenticationService;

	@EJB
	private DevicePolicyService devicePolicyService;

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
