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

import net.link.safeonline.authentication.exception.ExistingDeviceClassException;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.DeviceClass;
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
@Name("operDeviceClass")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "DeviceClassBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class DeviceClassBean implements DeviceClass {

	private static final Log LOG = LogFactory.getLog(DeviceClassBean.class);

	public static final String OPER_DEVICE_CLASS_LIST_NAME = "operDeviceClassList";

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	DeviceService deviceService;

	private String name;

	/*
	 * Seam Data models
	 */
	@DataModel(OPER_DEVICE_CLASS_LIST_NAME)
	public List<DeviceClassEntity> deviceClassList;

	@DataModelSelection(OPER_DEVICE_CLASS_LIST_NAME)
	@Out(value = "selectedDeviceClass", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private DeviceClassEntity selectedDeviceClass;

	/*
	 * Lifecyle
	 */
	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	/*
	 * Factories
	 */
	@Factory(OPER_DEVICE_CLASS_LIST_NAME)
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void deviceClassListFactory() {
		LOG.debug("device class list factory");
		this.deviceClassList = this.deviceService.listDeviceClasses();
	}

	/*
	 * Actions
	 */
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add device class: " + this.name);
		try {
			this.deviceService.addDeviceClass(this.name);
		} catch (ExistingDeviceClassException e) {
			LOG.debug("device class already exists: " + this.name);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorDeviceClassAlreadyExists", this.name);
			return null;
		}
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() {
		LOG.debug("remove device class: " + this.selectedDeviceClass.getName());
		return "success";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String view() {
		LOG.debug("view device class: " + this.selectedDeviceClass.getName());
		return "view";
	}

	/*
	 * Accessors
	 */
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getName() {
		return this.name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setName(String name) {
		this.name = name;
	}
}
