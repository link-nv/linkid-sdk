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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.DeviceRegistration;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
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
    private Log                   log;

    private String                device;

    private String                password;

    @In(required = true)
    private AuthenticationService authenticationService;

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private String                application;

    @In(value = LoginManager.REQUIRED_DEVICES_ATTRIBUTE, required = false)
    private Set<DeviceEntity>     requiredDevicePolicy;

    @EJB
    private DevicePolicyService   devicePolicyService;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
        this.device = null;
        this.password = null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String deviceNext()
            throws IOException, DeviceNotFoundException {

        this.log.debug("deviceNext: " + this.device);

        String registrationURL = this.devicePolicyService.getRegistrationURL(this.device);
        if (this.device.equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(registrationURL);
            return null;
        }
        AuthenticationUtils.redirect(registrationURL, this.device, this.userId);
        return null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getDevice() {

        return this.device;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public void setDevice(String device) {

        this.device = device;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getPassword() {

        return this.password;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String passwordNext()
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceDisabledException {

        this.log.debug("passwordNext");
        this.authenticationService.setPassword(this.userId, this.password);
        this.authenticationService.authenticate(getUsername(), this.password);
        super.relogin(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, this.authenticationService.getSsoCookie());
        return null;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public void setPassword(String password) {

        this.password = password;
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    public String getUsername() {

        return this.subjectService.getSubjectLogin(this.userId);
    }

    @RolesAllowed(AuthenticationConstants.USER_ROLE)
    @Factory("applicationDevicesDeviceRegistration")
    public List<SelectItem> applicationDevicesFactory()
            throws ApplicationNotFoundException, EmptyDevicePolicyException {

        this.log.debug("application devices factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<SelectItem> applicationDevices = new LinkedList<SelectItem>();

        List<DeviceEntity> devicePolicy = this.devicePolicyService.getDevicePolicy(this.application, this.requiredDevicePolicy);
        for (DeviceEntity deviceEntity : devicePolicy) {
            String deviceName = this.devicePolicyService.getDeviceDescription(deviceEntity.getName(), viewLocale);
            SelectItem applicationDevice = new SelectItem(deviceEntity.getName(), deviceName);
            this.log.debug("device " + deviceName + ": " + deviceEntity.isRegistrable() + " (path=" + deviceEntity.getRegistrationPath());
            applicationDevice.setDisabled(!deviceEntity.isRegistrable());
            applicationDevices.add(applicationDevice);
        }
        return applicationDevices;
    }

}
