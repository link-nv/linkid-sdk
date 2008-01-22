/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.device.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceDescriptionException;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.DeviceDescription;
import net.link.safeonline.service.DeviceService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("deviceDesc")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "DeviceDescriptionBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class DeviceDescriptionBean implements DeviceDescription {

	private static final Log LOG = LogFactory
			.getLog(DeviceDescriptionBean.class);

	public static final String OPER_DEVICE_DESCR_LIST_NAME = "deviceDescriptions";

	private String language;

	private String description;

	@In(value = "selectedDevice", required = true)
	private DeviceEntity selectedDevice;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private DeviceService deviceService;

	@DataModel(OPER_DEVICE_DESCR_LIST_NAME)
	public List<DeviceDescriptionEntity> deviceDescriptions;

	@DataModelSelection(OPER_DEVICE_DESCR_LIST_NAME)
	@Out(value = "selectedDeviceDescription", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private DeviceDescriptionEntity selectedDeviceDescription;

	/*
	 * Lifecycle
	 */
	@Remove
	@Destroy
	public void destroyCallback() {
	}

	/*
	 * Factories
	 */
	@Factory(OPER_DEVICE_DESCR_LIST_NAME)
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void deviceDescriptionsListFactory() {
		LOG.debug("device description list factory for device: "
				+ this.selectedDevice.getName());
		try {
			this.deviceDescriptions = this.deviceService
					.listDeviceDescriptions(this.selectedDevice.getName());
		} catch (DeviceNotFoundException e) {
			LOG.debug("device " + this.selectedDevice.getName() + " not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return;
		}
	}

	/*
	 * Actions
	 */
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add: " + this.language);

		DeviceDescriptionEntity newDeviceDescription = new DeviceDescriptionEntity();
		DeviceDescriptionPK pk = new DeviceDescriptionPK(this.selectedDevice
				.getName(), this.language);
		newDeviceDescription.setPk(pk);
		newDeviceDescription.setDescription(this.description);
		try {
			this.deviceService.addDeviceDescription(newDeviceDescription);
		} catch (DeviceNotFoundException e) {
			LOG.debug("device not found: " + this.selectedDevice.getName());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorDeviceNotFound");
			return null;
		} catch (ExistingDeviceDescriptionException e) {
			LOG.debug("device description already exists");
			this.facesMessages.addToControlFromResourceBundle("language",
					FacesMessage.SEVERITY_ERROR,
					"errorDeviceDescriptionAlreadyExists");
			return null;
		}
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Begin
	public String edit() {
		LOG.debug("edit: " + this.selectedDeviceDescription);
		return "edit-desc";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() {
		LOG.debug("remove: " + this.selectedDeviceDescription);
		try {
			this.deviceService
					.removeDeviceDescription(this.selectedDeviceDescription);
		} catch (DeviceDescriptionNotFoundException e) {
			String msg = "device description not found";
			LOG.debug(msg);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorDeviceDescriptionNotFound");
		}
		deviceDescriptionsListFactory();
		return "removed";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		LOG.debug("save: " + this.selectedDeviceDescription);
		this.deviceService
				.saveDeviceDescription(this.selectedDeviceDescription);
		return "saved";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String cancelEdit() {
		return "cancel";
	}

	/*
	 * Accessors
	 */
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getLanguage() {
		return this.language;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setLanguage(String language) {
		this.language = language;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getDescription() {
		return this.description;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setDescription(String description) {
		this.description = description;
	}
}
