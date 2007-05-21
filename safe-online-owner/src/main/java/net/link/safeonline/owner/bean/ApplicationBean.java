/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.owner.Application;
import net.link.safeonline.owner.DeviceEntry;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.service.DeviceService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("ownerApplication")
@LocalBinding(jndiBinding = OwnerConstants.JNDI_PREFIX
		+ "ApplicationBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class ApplicationBean implements Application {

	private static final Log LOG = LogFactory.getLog(ApplicationBean.class);

	@EJB
	private ApplicationService applicationService;

	@EJB
	private SubscriptionService subscriptionService;

	@EJB
	private DeviceService deviceService;

	@In(create = true)
	FacesMessages facesMessages;

	@SuppressWarnings("unused")
	@Out
	private long numberOfSubscriptions;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@SuppressWarnings("unused")
	@DataModel
	private List<ApplicationEntity> ownerApplicationList;

	@DataModelSelection("ownerApplicationList")
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	@In(value = "selectedApplication", required = false)
	private ApplicationEntity selectedApplication;

	@Out(value = "selectedApplicationIdentity", required = false)
	@SuppressWarnings("unused")
	private List<ApplicationIdentityAttributeEntity> selectedApplicationIdentity;

	@DataModel
	private List<DeviceEntry> allowedDevices;

	@Factory("ownerApplicationList")
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.ownerApplicationList = this.applicationService
				.getOwnedApplications();
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String view() {
		String applicationName = this.selectedApplication.getName();
		LOG.debug("view: " + applicationName);
		try {
			this.numberOfSubscriptions = this.subscriptionService
					.getNumberOfSubscriptions(applicationName);
			this.selectedApplicationIdentity = this.applicationService
					.getCurrentApplicationIdentity(applicationName);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		return "view-application";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String edit() {
		LOG.debug("edit: " + this.selectedApplication.getName());
		return "edit-application";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String save() {
		String applicationName = this.selectedApplication.getName();
		String applicationDescription = this.selectedApplication
				.getDescription();
		boolean deviceRestriction = this.selectedApplication
				.isDeviceRestriction();
		LOG.debug("save: " + applicationName);
		LOG.debug("description: " + applicationDescription);

		List<AllowedDeviceEntity> allowedDeviceList = new ArrayList<AllowedDeviceEntity>();
		for (DeviceEntry deviceEntry : this.allowedDevices) {
			if (deviceEntry.isAllowed() == true) {
				AllowedDeviceEntity device = new AllowedDeviceEntity(
						this.selectedApplication, deviceEntry.getDevice(),
						deviceEntry.getWeight());
				allowedDeviceList.add(device);
			}
		}

		try {
			this.applicationService.setApplicationDescription(applicationName,
					applicationDescription);
			this.applicationService.setApplicationDeviceRestriction(
					applicationName, deviceRestriction);
			this.deviceService.setAllowedDevices(this.selectedApplication,
					allowedDeviceList);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "saved";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String viewStats() {
		return "viewstats";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	@Factory("allowedDevices")
	public void allowedDevices() {
		if (selectedApplication == null) {
			return;
		}
		List<DeviceEntity> deviceList = this.deviceService.listDevices();
		List<AllowedDeviceEntity> allowedDeviceList = this.deviceService
				.listAllowedDevices(selectedApplication);

		this.allowedDevices = new ArrayList<DeviceEntry>();

		boolean defaultValue = false;

		for (DeviceEntity deviceEntity : deviceList) {
			this.allowedDevices.add(new DeviceEntry(deviceEntity, defaultValue,
					0));
		}

		for (AllowedDeviceEntity allowedDevice : allowedDeviceList) {
			for (DeviceEntry deviceEntry : this.allowedDevices) {
				if (deviceEntry.getDevice().equals(allowedDevice.getDevice())) {
					deviceEntry.setAllowed(true);
					deviceEntry.setWeight(allowedDevice.getWeight());
				}
			}
		}

	}
}
