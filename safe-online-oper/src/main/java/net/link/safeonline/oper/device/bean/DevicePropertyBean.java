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
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDevicePropertyException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.DevicePropertyPK;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.DeviceProperty;
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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("deviceProp")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "DevicePropertyBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DevicePropertyBean implements DeviceProperty {

    private static final Log          LOG                        = LogFactory.getLog(DevicePropertyBean.class);

    public static final String        OPER_DEVICE_PROP_LIST_NAME = "deviceProperties";

    private String                    name;

    private String                    value;

    @In(value = "selectedDevice", required = true)
    private DeviceEntity              selectedDevice;

    @In(create = true)
    FacesMessages                     facesMessages;

    @EJB
    private DeviceService             deviceService;

    @DataModel(OPER_DEVICE_PROP_LIST_NAME)
    public List<DevicePropertyEntity> deviceProperties;

    @DataModelSelection(OPER_DEVICE_PROP_LIST_NAME)
    @Out(value = "selectedDeviceProperty", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private DevicePropertyEntity      selectedDeviceProperty;


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
    @Factory(OPER_DEVICE_PROP_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void devicePropertiesListFactory() throws DeviceNotFoundException {

        LOG.debug("device properties list factory for device: " + this.selectedDevice.getName());
        this.deviceProperties = this.deviceService.listDeviceProperties(this.selectedDevice.getName());
    }

    /*
     * Actions
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( { @Error(exceptionClass = ExistingDevicePropertyException.class, messageId = "errorDevicePropertyAlreadyExists", fieldId = "name") })
    public String add() throws ExistingDevicePropertyException, DeviceNotFoundException {

        LOG.debug("add: " + this.name);

        DevicePropertyEntity newDeviceProperty = new DevicePropertyEntity();
        DevicePropertyPK pk = new DevicePropertyPK(this.selectedDevice.getName(), this.name);
        newDeviceProperty.setPk(pk);
        newDeviceProperty.setValue(this.value);

        this.deviceService.addDeviceProperty(newDeviceProperty);

        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Begin
    public String edit() {

        LOG.debug("edit: " + this.selectedDeviceProperty);
        return "edit-prop";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove() throws DevicePropertyNotFoundException, DeviceNotFoundException {

        LOG.debug("remove: " + this.selectedDeviceProperty);
        this.deviceService.removeDeviceProperty(this.selectedDeviceProperty);
        devicePropertiesListFactory();
        return "removed";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() {

        LOG.debug("save: " + this.selectedDeviceProperty);
        this.deviceService.saveDeviceProperty(this.selectedDeviceProperty);
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
    public String getName() {

        return this.name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setName(String name) {

        this.name = name;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getValue() {

        return this.value;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setValue(String value) {

        this.value = value;
    }

}
