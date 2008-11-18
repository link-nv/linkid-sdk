/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.bean;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.device.backend.MobileManager;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.Registration;
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
@Name("registration")
@LocalBinding(jndiBinding = Registration.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class RegistrationBean implements Registration {

    @Logger
    private Log                  log;

    @In(create = true)
    FacesMessages                facesMessages;

    private String               mobile;

    private String               mobileActivationCode;

    private String               mobileOTP;

    private String               challengeId;

    @In(value = ProtocolContext.PROTOCOL_CONTEXT)
    private ProtocolContext      protocolContext;

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    private EncapDeviceService   encapDeviceService;

    @EJB(mappedName = MobileManager.JNDI_BINDING)
    private MobileManager        mobileManager;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService samlAuthorityService;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy");
        reset();
    }

    private void reset() {

        this.mobile = null;
        this.mobileActivationCode = null;
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
    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed") })
    public String mobileRegister()
            throws MobileException, MalformedURLException, MobileRegistrationException {

        this.log.debug("register mobile: " + this.mobile);
        return mobileActivation();
    }

    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed") })
    public String mobileActivationRetry()
            throws MalformedURLException, MobileException, MobileRegistrationException {

        this.log.debug("mobile retry activation: " + this.mobile);
        return mobileActivation();
    }

    private String mobileActivation()
            throws MalformedURLException, MobileException, MobileRegistrationException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpSession session = (HttpSession) externalContext.getSession(true);
        String sessionId = session.getId();
        this.mobileActivationCode = this.encapDeviceService.register(this.mobile, sessionId);
        return "";
    }

    public String mobileActivationOk() {

        this.log.debug("mobile activation ok: " + this.mobile);
        return "authenticate";
    }

    public String requestOTP()
            throws MalformedURLException, MobileException {

        this.log.debug("request OTP: mobile=" + this.mobile);
        this.challengeId = this.encapDeviceService.requestOTP(this.mobile);
        this.log.debug("received challengeId: " + this.challengeId);
        return "success";
    }

    @End
    public String authenticate()
            throws IOException, MobileException, SubjectNotFoundException, AttributeTypeNotFoundException {

        boolean result = this.encapDeviceService.authenicateEncap(this.challengeId, this.mobileOTP);
        if (false == result) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
            return null;
        }

        // encap registration and authentication was successful, commit this
        // registration to OLAS.
        this.encapDeviceService.commitRegistration(this.protocolContext.getSubject(), this.mobile);

        this.protocolContext.setSuccess(true);
        exit();
        return null;
    }

    public String getMobile() {

        return this.mobile;
    }

    public void setMobile(String mobile) {

        this.mobile = mobile;
    }

    public String getMobileActivationCode() {

        return this.mobileActivationCode;
    }

    public String getMobileClientLink() {

        return this.mobileManager.getClientDownloadLink();
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
