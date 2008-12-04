/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.Enable;
import net.link.safeonline.model.encap.EncapDeviceService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("enable")
@LocalBinding(jndiBinding = Enable.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class EnableBean implements Enable {

    @Logger
    private Log                  log;

    @In(create = true)
    FacesMessages                facesMessages;

    private PhoneNumber          mobile;

    private String               mobileOTP;

    private String               challengeId;

    @In(value = ProtocolContext.PROTOCOL_CONTEXT)
    private ProtocolContext      protocolContext;

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    private EncapDeviceService   encapDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService samlAuthorityService;


    @PostConstruct
    public void init() {

        // need to explicitly lookup this session param as the @In annotation has not been performed ...
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.protocolContext = (ProtocolContext) ((HttpSession) facesContext.getExternalContext().getSession(false))
                                                                                                                    .getAttribute(ProtocolContext.PROTOCOL_CONTEXT);
        this.mobile = new PhoneNumber(this.protocolContext.getAttribute());
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
        reset();
    }

    private void reset() {

        this.mobile = null;
        this.mobileOTP = null;
        this.challengeId = null;
    }

    @End
    public String cancel()
            throws IOException {

        this.protocolContext.setSuccess(false);
        exit();
        return null;
    }

    private void exit()
            throws IOException {

        this.log.debug("exit");
        reset();
        this.protocolContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.redirect("./deviceexit");
    }

    @Begin
    public String requestOTP()
            throws MalformedURLException, MobileException {

        this.log.debug("request OTP: mobile=" + this.mobile);
        this.challengeId = this.encapDeviceService.requestOTP(this.mobile.getNumber());
        this.log.debug("received challengeId: " + this.challengeId);
        return "success";
    }

    @End
    public String authenticate()
            throws IOException, MobileException, SubjectNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException,
            AttributeNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException {

        boolean result = this.encapDeviceService.authenicateEncap(this.challengeId, this.mobileOTP);
        if (false == result) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
            return null;
        }

        // encap authentication was successful, enabled this registration.
        this.encapDeviceService.enable(this.protocolContext.getSubject(), this.mobile.getNumber());

        this.protocolContext.setSuccess(true);
        exit();
        return null;
    }

    public PhoneNumber getMobile() {

        return this.mobile;
    }

    public void setMobile(PhoneNumber mobile) {

        this.mobile = mobile;
    }

    public String getMobileOTP() {

        return this.mobileOTP;
    }

    public void setMobileOTP(String mobileOTP) {

        this.mobileOTP = mobileOTP;
    }

    public String getChallengeId() {

        return this.challengeId;
    }

}
