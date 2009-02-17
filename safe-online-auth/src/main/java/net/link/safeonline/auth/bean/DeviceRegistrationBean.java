/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.DeviceRegistration;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.DeviceEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;


@Stateful
@Name("deviceRegistration")
@LocalBinding(jndiBinding = DeviceRegistration.JNDI_BINDING)
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceRegistrationBean extends AbstractLoginBean implements DeviceRegistration {

    @Logger
    private Log                 log;

    private String              device;

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private long                application;

    @In(value = LoginManager.REQUIRED_DEVICES_ATTRIBUTE, required = false)
    private Set<DeviceEntity>   requiredDevicePolicy;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService devicePolicyService;


    @Remove
    @Destroy
    public void destroyCallback() {

        log.debug("destroy");
        device = null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String deviceNext()
            throws IOException, DeviceNotFoundException {

        log.debug("deviceNext: " + device);

        String registrationURL = devicePolicyService.getRegistrationURL(device);
        AuthenticationUtils.redirect(registrationURL, device, userId);
        return null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getDevice() {

        return device;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public void setDevice(String device) {

        this.device = device;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getUsername() {

        return subjectService.getSubjectLogin(userId);
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    @Factory("applicationDevicesDeviceRegistration")
    public List<SelectItem> applicationDevicesFactory()
            throws ApplicationNotFoundException, EmptyDevicePolicyException {

        log.debug("application devices factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<SelectItem> applicationDevices = new LinkedList<SelectItem>();

        List<DeviceEntity> devicePolicy = devicePolicyService.getDevicePolicy(application, requiredDevicePolicy);
        for (DeviceEntity deviceEntity : devicePolicy) {
            String deviceName = devicePolicyService.getDeviceDescription(deviceEntity.getName(), viewLocale);
            SelectItem applicationDevice = new SelectItem(deviceEntity.getName(), deviceName);
            log.debug("device " + deviceName + ": " + deviceEntity.isRegistrable() + " (path=" + deviceEntity.getRegistrationPath());
            applicationDevice.setDisabled(!deviceEntity.isRegistrable());
            applicationDevices.add(applicationDevice);
        }
        return applicationDevices;
    }

}
