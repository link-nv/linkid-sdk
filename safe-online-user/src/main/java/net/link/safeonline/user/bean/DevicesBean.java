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

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.data.DeviceMappingDO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.user.DeviceEntry;
import net.link.safeonline.user.Devices;
import net.link.safeonline.user.UserConstants;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

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
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Name("devicesBean")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "DevicesBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class DevicesBean implements Devices {

	private static final Log LOG = LogFactory.getLog(DevicesBean.class);

	private static final String DEVICES_LIST_NAME = "devices";

	private static final String DEVICE_MAPPINGS_LIST_NAME = "deviceMappings";

	private String oldPassword;

	private String newPassword;

	private boolean credentialCacheFlushRequired;

	@DataModel(DEVICES_LIST_NAME)
	List<DeviceEntry> devices;

	@DataModelSelection(DEVICES_LIST_NAME)
	private DeviceEntry selectedDevice;

	@DataModel(DEVICE_MAPPINGS_LIST_NAME)
	List<DeviceMappingDO> deviceMappings;

	@DataModelSelection(DEVICE_MAPPINGS_LIST_NAME)
	private DeviceMappingDO selectedDeviceMapping;

	@PostConstruct
	public void postConstructCallback() {
		this.credentialCacheFlushRequired = false;
	}

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private DeviceService deviceService;

	@EJB
	private CredentialService credentialService;

	@EJB
	private DevicePolicyService devicePolicyService;

	@EJB
	private NodeAuthenticationService nodeAuthenticationService;

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
	public String registerPassword() {
		try {
			this.credentialService.registerPassword(this.newPassword);
		} catch (PermissionDeniedException e) {
			String msg = "old password not correct";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("newpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotCorrect");
			return null;
		} catch (DeviceNotFoundException e) {
			String msg = "there is no old password";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("newpassword",
					FacesMessage.SEVERITY_ERROR, "errorOldPasswordNotFound");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		}

		this.credentialCacheFlushRequired = true;
		LOG.debug("returning success");
		return "success";

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
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
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
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
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
	@Factory(DEVICE_MAPPINGS_LIST_NAME)
	public List<DeviceMappingDO> deviceRegistrationsFactory() {
		Locale locale = getViewLocale();
		LOG.debug("device registrations factory");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		try {
			this.deviceMappings = this.deviceService.getDeviceRegistrations(
					subject, locale);
		} catch (SubjectNotFoundException e) {
			LOG.debug("Subject not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		} catch (DeviceNotFoundException e) {
			LOG.debug("Device not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			LOG.error("permission denied: " + e.getMessage());
			return null;
		} catch (AttributeTypeNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			LOG.error("attribute type not found");
			return null;
		}
		return this.deviceMappings;
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
		try {
			registrationURL += "?source=user&node=" + getNodeName();
		} catch (NodeNotFoundException e) {
			LOG.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
			return null;
		}

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
	public String removeDevice() {
		if (!deviceRemovalAllowed()) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return null;
		}
		LOG.debug("remove device: "
				+ this.selectedDeviceMapping.getFriendlyName());
		return redirectRemove(this.selectedDeviceMapping.getDeviceMapping()
				.getDevice().getName());
	}

	private boolean deviceRemovalAllowed() {
		if (this.deviceMappings.size() == 1)
			return false;
		return true;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String remove() {
		LOG.debug("remove device: " + this.selectedDevice.getFriendlyName());
		return redirectRemove(this.selectedDevice.getDevice().getName());
	}

	private String redirectRemove(String deviceName) {
		String removalURL;
		try {
			removalURL = this.devicePolicyService.getRemovalURL(deviceName);
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: " + deviceName);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
		try {
			removalURL += "?source=user&node=" + getNodeName();
		} catch (NodeNotFoundException e) {
			LOG.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
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
	public String updateDevice() {
		LOG.debug("update device: "
				+ this.selectedDeviceMapping.getFriendlyName());
		return redirectUpdate(this.selectedDeviceMapping.getDeviceMapping()
				.getDevice().getName());

	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String update() {
		LOG.debug("update device: " + this.selectedDevice.getFriendlyName());
		return redirectUpdate(this.selectedDevice.getDevice().getName());
	}

	private String redirectUpdate(String deviceName) {
		String updateURL;
		try {
			updateURL = this.devicePolicyService.getUpdateURL(deviceName);
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: " + deviceName);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		}
		try {
			updateURL += "?source=user&node=" + getNodeName();
		} catch (NodeNotFoundException e) {
			LOG.debug("node not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorNodeNotFound");
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

	private String getNodeName() throws NodeNotFoundException {
		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		return this.nodeAuthenticationService
				.authenticate(authIdentityServiceClient.getCertificate());
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public boolean isPasswordConfigured() {
		boolean hasPassword;
		try {
			hasPassword = this.credentialService.isPasswordConfigured();
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return false;
		} catch (DeviceNotFoundException e) {
			LOG.error("device not found: "
					+ this.selectedDevice.getDevice().getName());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return false;
		}
		return hasPassword;
	}
}
