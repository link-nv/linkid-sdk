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

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.auth.AccountRegistration;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;


@Stateful
@Name("accountRegistration")
@LocalBinding(jndiBinding = AccountRegistration.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class AccountRegistrationBean extends AbstractLoginBean implements AccountRegistration {

    @EJB(mappedName = UserRegistrationService.JNDI_BINDING)
    private UserRegistrationService userRegistrationService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService     devicePolicyService;

    @Logger
    private Log                     log;

    private String                  login;

    private String                  device;

    @In(required = false, value = com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY, scope = ScopeType.SESSION)
    String                          validCaptcha;

    private String                  givenCaptcha;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
    }

    public String getLogin() {

        return this.login;
    }

    public void setLogin(String login) {

        this.login = login;
    }

    @ErrorHandling( { @Error(exceptionClass = ExistingUserException.class, messageId = "errorLoginTaken", fieldId = "login"),
            @Error(exceptionClass = AttributeTypeNotFoundException.class, messageId = "errorLoginTaken", fieldId = "login"),
            @Error(exceptionClass = AttributeUnavailableException.class, messageId = "errorLoginTaken", fieldId = "login"),
            @Error(exceptionClass = PermissionDeniedException.class, messageId = "errorPermissionDenied", fieldId = "login") })
    public String loginNext()
            throws ExistingUserException, AttributeTypeNotFoundException, PermissionDeniedException, AttributeUnavailableException {

        this.log.debug("loginNext");

        HelpdeskLogger.add("account creation: login=" + this.login, LogLevelType.INFO);

        this.log.debug("valid captcha: " + this.validCaptcha);
        this.log.debug("given captcha: " + this.givenCaptcha);

        if (null == this.validCaptcha) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorNoCaptcha");
            return null;
        }

        if (!this.validCaptcha.equals(this.givenCaptcha)) {
            this.facesMessages.addToControlFromResourceBundle("captcha", FacesMessage.SEVERITY_ERROR, "errorInvalidCaptcha");
            this.givenCaptcha = null;
            return null;
        }

        SubjectEntity subject = this.userRegistrationService.registerUser(this.login);

        this.userId = subject.getUserId();
        return "next";
    }

    public String deviceNext()
            throws DeviceNotFoundException, IOException {

        this.log.debug("deviceNext: " + this.device);

        HelpdeskLogger.add("account creation: register device: " + this.device, LogLevelType.INFO);

        String registrationURL = this.devicePolicyService.getRegistrationURL(this.device);

        AuthenticationUtils.redirect(registrationURL, this.device, this.userId);
        return null;
    }

    public String getDevice() {

        return this.device;
    }

    public void setDevice(String device) {

        this.device = device;
    }

    public String getGivenCaptcha() {

        return this.givenCaptcha;
    }

    public void setGivenCaptcha(String givenCaptcha) {

        this.givenCaptcha = givenCaptcha;
    }

    public String getCaptchaURL() {

        return "/captcha.jpg?cacheid=" + Math.random() * 1000000;
    }

    public String getUsername() {

        return this.subjectService.getSubjectLogin(this.userId);
    }

    @Factory("allDevicesAccountRegistration")
    public List<SelectItem> allDevicesFactory()
            throws ApplicationNotFoundException, EmptyDevicePolicyException {

        this.log.debug("all devices factory");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        List<SelectItem> allDevices = new LinkedList<SelectItem>();

        List<DeviceEntity> devices = this.devicePolicyService.getDevices();

        for (DeviceEntity deviceEntity : devices) {
            String deviceName = this.devicePolicyService.getDeviceDescription(deviceEntity.getName(), viewLocale);
            SelectItem allDevice = new SelectItem(deviceEntity.getName(), deviceName);
            allDevice.setDisabled(!deviceEntity.isRegistrable());
            allDevices.add(allDevice);
        }
        return allDevices;
    }
}
