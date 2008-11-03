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

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.Device;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("device")
@LocalBinding(jndiBinding = Device.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class DeviceBean implements Device {

    @In(create = true)
    FacesMessages               facesMessages;

    @Logger
    private Log                 log;

    @EJB
    private DevicePolicyService devicePolicyService;

    @In(value = LoginManager.APPLICATION_ID_ATTRIBUTE, required = true)
    private String              application;

    @In(value = LoginManager.REQUIRED_DEVICES_ATTRIBUTE, required = false)
    private Set<DeviceEntity>   requiredDevicePolicy;

    @Out(required = false, scope = ScopeType.SESSION)
    private String              deviceSelection;


    @Remove
    @Destroy
    public void destroyCallback() {

    }

    public String getSelection() {

        return this.deviceSelection;
    }

    public void setSelection(String deviceSelection) {

        this.deviceSelection = deviceSelection;
    }

    public String next() throws IOException, DeviceNotFoundException {

        this.log.debug("next: " + this.deviceSelection);
        HelpdeskLogger.add("selected authentication device: " + this.deviceSelection, LogLevelType.INFO);

        String authenticationPath = this.devicePolicyService.getAuthenticationURL(this.deviceSelection);
        this.log.debug("authenticationPath: " + authenticationPath);

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        String requestPath = ((HttpServletRequest) externalContext.getRequest()).getRequestURL().toString();

        if (!this.deviceSelection.equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID))
            return AuthenticationUtils.redirectAuthentication(requestPath, authenticationPath, this.deviceSelection);

        externalContext.redirect(authenticationPath);
        return null;
    }

    @Factory("applicationDevices")
    public List<SelectItem> applicationDevicesFactory() throws ApplicationNotFoundException, EmptyDevicePolicyException {

        this.log.debug("application devices factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<SelectItem> applicationDevices = new LinkedList<SelectItem>();

        List<DeviceEntity> devicePolicy = this.devicePolicyService.getDevicePolicy(this.application, this.requiredDevicePolicy);
        for (DeviceEntity device : devicePolicy) {
            String deviceName = this.devicePolicyService.getDeviceDescription(device.getName(), viewLocale);
            SelectItem applicationDevice = new SelectItem(device.getName(), deviceName);
            applicationDevices.add(applicationDevice);
        }
        return applicationDevices;
    }

    @Factory("allDevices")
    public List<SelectItem> allDevicesFactory() {

        this.log.debug("application devices factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<SelectItem> allDevices = new LinkedList<SelectItem>();

        List<DeviceEntity> devices = this.devicePolicyService.getDevices();

        for (DeviceEntity device : devices) {
            String deviceName = this.devicePolicyService.getDeviceDescription(device.getName(), viewLocale);
            SelectItem allDevice = new SelectItem(device.getName(), deviceName);
            allDevices.add(allDevice);
        }
        return allDevices;
    }
}
