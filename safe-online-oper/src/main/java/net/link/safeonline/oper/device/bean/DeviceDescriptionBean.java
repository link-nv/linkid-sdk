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

import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceDescriptionException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("deviceDesc")
@LocalBinding(jndiBinding = DeviceDescription.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceDescriptionBean implements DeviceDescription {

    private static final Log             LOG                         = LogFactory.getLog(DeviceDescriptionBean.class);

    public static final String           OPER_DEVICE_DESCR_LIST_NAME = "deviceDescriptions";

    private String                       language;

    private String                       description;

    @In(value = "selectedDevice", required = true)
    private DeviceEntity                 selectedDevice;

    @In(create = true)
    FacesMessages                        facesMessages;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    private DeviceService                deviceService;

    @DataModel(OPER_DEVICE_DESCR_LIST_NAME)
    public List<DeviceDescriptionEntity> deviceDescriptions;

    @DataModelSelection(OPER_DEVICE_DESCR_LIST_NAME)
    @Out(value = "selectedDeviceDescription", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private DeviceDescriptionEntity      selectedDeviceDescription;


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
    public void deviceDescriptionsListFactory()
            throws DeviceNotFoundException {

        LOG.debug("device description list factory for device: " + this.selectedDevice.getName());
        this.deviceDescriptions = this.deviceService.listDeviceDescriptions(this.selectedDevice.getName());
    }

    /*
     * Actions
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( { @Error(exceptionClass = ExistingDeviceDescriptionException.class, messageId = "errorDeviceDescriptionAlreadyExists", fieldId = "language") })
    public String add()
            throws ExistingDeviceDescriptionException, DeviceNotFoundException {

        LOG.debug("add: " + this.language);

        DeviceDescriptionEntity newDeviceDescription = new DeviceDescriptionEntity();
        DeviceDescriptionPK pk = new DeviceDescriptionPK(this.selectedDevice.getName(), this.language);
        newDeviceDescription.setPk(pk);
        newDeviceDescription.setDescription(this.description);
        this.deviceService.addDeviceDescription(newDeviceDescription);
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
    public String remove()
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException {

        LOG.debug("remove: " + this.selectedDeviceDescription);
        this.deviceService.removeDeviceDescription(this.selectedDeviceDescription);
        deviceDescriptionsListFactory();
        return "removed";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() {

        LOG.debug("save: " + this.selectedDeviceDescription);
        this.deviceService.saveDeviceDescription(this.selectedDeviceDescription);
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
