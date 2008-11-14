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
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
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
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("operDeviceClass")
@LocalBinding(jndiBinding = DeviceClass.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceClassBean implements DeviceClass {

    private static final Log       LOG                         = LogFactory.getLog(DeviceClassBean.class);

    public static final String     OPER_DEVICE_CLASS_LIST_NAME = "operDeviceClassList";

    @In(create = true)
    FacesMessages                  facesMessages;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    DeviceService                  deviceService;

    private String                 name;

    private String                 authenticationContextClass;

    /*
     * Seam Data models
     */
    @DataModel(OPER_DEVICE_CLASS_LIST_NAME)
    public List<DeviceClassEntity> deviceClassList;

    @DataModelSelection(OPER_DEVICE_CLASS_LIST_NAME)
    @Out(value = "selectedDeviceClass", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private DeviceClassEntity      selectedDeviceClass;


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
            this.deviceService.addDeviceClass(this.name, this.authenticationContextClass);
        } catch (ExistingDeviceClassException e) {
            LOG.debug("device class already exists: " + this.name);
            this.facesMessages.addToControlFromResourceBundle("name", FacesMessage.SEVERITY_ERROR, "errorDeviceClassAlreadyExists",
                    this.name);
            return null;
        }
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove() {

        LOG.debug("remove device class: " + this.selectedDeviceClass.getName());
        try {
            this.deviceService.removeDeviceClass(this.selectedDeviceClass.getName());
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
            return null;
        }
        deviceClassListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view device class: " + this.selectedDeviceClass.getName());
        return "view";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String edit() {

        LOG.debug("edit device class: " + this.selectedDeviceClass.getName());
        this.authenticationContextClass = this.selectedDeviceClass.getAuthenticationContextClass();
        return "edit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save()
            throws DeviceClassNotFoundException {

        LOG.debug("save device class: " + this.selectedDeviceClass.getName());
        String deviceClassName = this.selectedDeviceClass.getName();
        this.deviceService.updateAuthenticationContextClass(deviceClassName, this.authenticationContextClass);
        this.selectedDeviceClass = this.deviceService.getDeviceClass(deviceClassName);
        return "success";
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
    public String getAuthenticationContextClass() {

        return this.authenticationContextClass;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAuthenticationContextClass(String authenticationContextClass) {

        this.authenticationContextClass = authenticationContextClass;
    }
}
