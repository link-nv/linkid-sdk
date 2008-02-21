/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.LastDeviceException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.user.DeviceEntry;
import net.link.safeonline.user.Devices;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("devicesBean")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "DevicesBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class DevicesBean implements Devices {

	private static final Log LOG = LogFactory.getLog(DevicesBean.class);

	private static final String DEVICES_LIST_NAME = "devices";

	private static final String MOBILE_WEAK_ATTRIBUTE_LIST_NAME = "mobileWeakAttributes";

	private String oldPassword;

	private String newPassword;

	private boolean credentialCacheFlushRequired;

	@DataModel(DEVICES_LIST_NAME)
	List<DeviceEntry> devices;

	@DataModelSelection(DEVICES_LIST_NAME)
	private DeviceEntry selectedDevice;

	@DataModel(MOBILE_WEAK_ATTRIBUTE_LIST_NAME)
	List<AttributeDO> mobileWeakAttributes;

	@SuppressWarnings("unused")
	@DataModelSelection(MOBILE_WEAK_ATTRIBUTE_LIST_NAME)
	private AttributeDO selectedMobile;

	@PostConstruct
	public void postConstructCallback() {
		this.credentialCacheFlushRequired = false;
	}

	@EJB
	private CredentialService credentialService;

	@EJB
	private IdentityService identityService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	public String getNewPassword() {
		return this.newPassword;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return this.oldPassword;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String changePassword() {
		try {
			this.credentialService.changePassword(this.oldPassword,
					this.newPassword);
		} catch (PermissionDeniedException e) {
			String msg = "old password not correct";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("oldpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotCorrect");
			return null;
		} catch (DeviceNotFoundException e) {
			String msg = "there is no old password";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("oldpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotFound");
			return null;
		}

		this.credentialCacheFlushRequired = true;
		LOG.debug("returning success");
		return "success";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String removePassword() {
		try {
			this.credentialService.removePassword(this.oldPassword);
		} catch (PermissionDeniedException e) {
			String msg = "old password not correct";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("oldpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotCorrect");
			return null;
		} catch (DeviceNotFoundException e) {
			String msg = "there is no old password";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("oldpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotFound");
			return null;
		} catch (LastDeviceException e) {
			LOG.debug(e.getMessage());
			this.facesMessages.addToControlFromResourceBundle("oldpassword",
					FacesMessage.SEVERITY_ERROR, "errorLastDevice");
			return null;
		}

		this.credentialCacheFlushRequired = true;
		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy callback");
		if (this.credentialCacheFlushRequired) {
			/*
			 * We will set a HTTP session attribute to communicate to the JAAS
			 * Login Filter that the credential cache for the caller principal
			 * needs to be flushed.
			 */
			try {
				/*
				 * The JACC spec is not really clear here whether we can
				 * retrieve the HttpServletRequest also from within the EJB
				 * container, or only from within the Servlet container.
				 */
				HttpServletRequest httpServletRequest = (HttpServletRequest) PolicyContext
						.getContext(HttpServletRequest.class.getName());
				if (null != httpServletRequest) {
					HttpSession session = httpServletRequest.getSession();
					String attributeName = "FlushJBossCredentialCache";
					session.setAttribute(attributeName,
							UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN);
					LOG.debug("setting " + attributeName);
				} else {
					LOG.debug("JACC HttpServletRequest is null");
				}
			} catch (PolicyContextException e) {
				LOG.error("JACC policy context error: " + e.getMessage());
				throw new EJBException("JACC policy context error: "
						+ e.getMessage());
			}
		}
		this.oldPassword = null;
		this.newPassword = null;
		this.credentialCacheFlushRequired = false;
	}

	private Locale getViewLocale() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		return viewLocale;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory("beidAttributes")
	public List<AttributeDO> beidAttributesFactory() {
		Locale locale = getViewLocale();
		List<AttributeDO> beidAttributes;
		try {
			beidAttributes = this.identityService.listAttributes(
					SafeOnlineConstants.BEID_DEVICE_ID, locale);
		} catch (DeviceNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			LOG.error("device not found");
			return new LinkedList<AttributeDO>();
		}
		return beidAttributes;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(MOBILE_WEAK_ATTRIBUTE_LIST_NAME)
	public List<AttributeDO> mobileWeakAttributesFactory() {
		Locale locale = getViewLocale();
		try {
			this.mobileWeakAttributes = this.identityService.listAttributes(
					SafeOnlineConstants.ENCAP_DEVICE_ID, locale);
		} catch (DeviceNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			LOG.error("device not found");
			return new LinkedList<AttributeDO>();
		}
		return this.mobileWeakAttributes;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(DEVICES_LIST_NAME)
	public List<DeviceEntry> devicesFactory() {
		Locale locale = getViewLocale();
		this.devices = new LinkedList<DeviceEntry>();
		List<DeviceEntity> deviceList = this.devicePolicyService.getDevices();
		for (DeviceEntity device : deviceList) {
			String deviceDescription = this.devicePolicyService
					.getDeviceDescription(device.getName(), locale);
			this.devices.add(new DeviceEntry(device, deviceDescription));
		}
		return this.devices;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String register() {
		LOG.debug("register device: " + this.selectedDevice.getFriendlyName());
		String registrationURL;
		try {
			registrationURL = this.devicePolicyService
					.getRegistrationURL(this.selectedDevice.getDevice()
							.getName());
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: "
					+ this.selectedDevice.getDevice().getName());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
		registrationURL += "?source=user";

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect(registrationURL);
		} catch (IOException e) {
			LOG.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
		}
		return null;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String remove() {
		LOG.debug("remove device: " + this.selectedDevice.getFriendlyName());
		String removalURL;
		try {
			removalURL = this.devicePolicyService
					.getRemovalURL(this.selectedDevice.getDevice().getName());
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: "
					+ this.selectedDevice.getDevice().getName());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect(removalURL);
		} catch (IOException e) {
			LOG.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
		}
		return null;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String update() {
		LOG.debug("update device: " + this.selectedDevice.getFriendlyName());
		String updateURL;
		try {
			updateURL = this.devicePolicyService
					.getUpdateURL(this.selectedDevice.getDevice().getName());
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: "
					+ this.selectedDevice.getDevice().getName());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		try {
			externalContext.redirect(updateURL);
		} catch (IOException e) {
			LOG.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
		}
		return null;

	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public boolean isPasswordConfigured() {
		boolean hasPassword = this.credentialService.isPasswordConfigured();
		return hasPassword;
	}
}
