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

import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassDescriptionException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassDescriptionPK;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.device.DeviceClassDescription;
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
@Name("deviceClassDesc")
@LocalBinding(jndiBinding = DeviceClassDescription.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceClassDescriptionBean implements DeviceClassDescription {

    private static final Log                  LOG                               = LogFactory.getLog(DeviceClassDescriptionBean.class);

    public static final String                OPER_DEVICE_CLASS_DESCR_LIST_NAME = "deviceClassDescriptions";

    private String                            language;

    private String                            description;

    @In(value = "selectedDeviceClass", required = true)
    private DeviceClassEntity                 selectedDeviceClass;

    @In(create = true)
    FacesMessages                             facesMessages;

    @EJB
    private DeviceService                     deviceService;

    @DataModel(OPER_DEVICE_CLASS_DESCR_LIST_NAME)
    public List<DeviceClassDescriptionEntity> deviceClassDescriptions;

    @DataModelSelection(OPER_DEVICE_CLASS_DESCR_LIST_NAME)
    @Out(value = "selectedDeviceClassDescription", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private DeviceClassDescriptionEntity      selectedDeviceClassDescription;


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
    @Factory(OPER_DEVICE_CLASS_DESCR_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void deviceClassDescriptionsListFactory()
            throws DeviceClassNotFoundException {

        LOG.debug("device class description list factory for device: " + this.selectedDeviceClass.getName());
        this.deviceClassDescriptions = this.deviceService.listDeviceClassDescriptions(this.selectedDeviceClass.getName());
    }

    /*
     * Actions
     */
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @ErrorHandling( { @Error(exceptionClass = ExistingDeviceClassDescriptionException.class, messageId = "errorDeviceClassDescriptionAlreadyExists", fieldId = "language") })
    public String add()
            throws ExistingDeviceClassDescriptionException, DeviceClassNotFoundException {

        LOG.debug("add: " + this.language);

        DeviceClassDescriptionEntity newDeviceClassDescription = new DeviceClassDescriptionEntity();
        DeviceClassDescriptionPK pk = new DeviceClassDescriptionPK(this.selectedDeviceClass.getName(), this.language);
        newDeviceClassDescription.setPk(pk);
        newDeviceClassDescription.setDescription(this.description);
        this.deviceService.addDeviceClassDescription(newDeviceClassDescription);
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Begin
    public String edit() {

        LOG.debug("edit: " + this.selectedDeviceClassDescription);
        return "edit-desc";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove()
            throws DeviceClassDescriptionNotFoundException, DeviceClassNotFoundException {

        LOG.debug("remove: " + this.selectedDeviceClassDescription);
        this.deviceService.removeDeviceClassDescription(this.selectedDeviceClassDescription);
        deviceClassDescriptionsListFactory();
        return "removed";
    }

    @End
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save() {

        LOG.debug("save: " + this.selectedDeviceClassDescription);
        this.deviceService.saveDeviceClassDescription(this.selectedDeviceClassDescription);
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
