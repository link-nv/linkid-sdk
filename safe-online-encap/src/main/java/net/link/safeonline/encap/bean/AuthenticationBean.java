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

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.encap.Authentication;
import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("authentication")
@LocalBinding(jndiBinding = Authentication.JNDI_BINDING)
@Interceptors(ErrorMessageInterceptor.class)
public class AuthenticationBean implements Authentication {

    private static final Log     LOG = LogFactory.getLog(AuthenticationBean.class);

    @In(create = true)
    FacesMessages                facesMessages;

    @In(value = AuthenticationContext.AUTHENTICATION_CONTEXT)
    AuthenticationContext        authenticationContext;

    @EJB
    private EncapDeviceService   encapDeviceService;

    @EJB
    private SamlAuthorityService samlAuthorityService;

    private String               challengeId;

    private String               mobile;

    private String               mobileOTP;


    public String getMobileOTP() {

        return this.mobileOTP;
    }

    public void setMobileOTP(String mobileOTP) {

        this.mobileOTP = mobileOTP;
    }

    public String getMobile() {

        return this.mobile;
    }

    public void setMobile(String mobile) {

        this.mobile = mobile;
    }

    public String getChallengeId() {

        return this.challengeId;
    }

    public void setChallengeId(String challengeId) {

        this.challengeId = challengeId;
    }

    @End
    public String login() throws MobileAuthenticationException, IOException {

        LOG.debug("login: " + this.mobile);
        HelpdeskLogger.add("login: " + this.mobile, LogLevelType.INFO);
        try {
            String userId = this.encapDeviceService.authenticate(this.mobile, this.challengeId, this.mobileOTP);
            if (null == userId) {
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
                HelpdeskLogger.add("login failed: " + this.mobile, LogLevelType.ERROR);
                return null;
            }
            login(userId);
        } catch (SubjectNotFoundException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
            HelpdeskLogger.add("login: subject not found for " + this.mobile, LogLevelType.ERROR);
            return null;
        } catch (MalformedURLException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
            HelpdeskLogger.add("login: encap webservice not available", LogLevelType.ERROR);
            return null;
        } catch (MobileException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
            HelpdeskLogger.add("login: failed to contact encap webservice for " + this.mobile, LogLevelType.ERROR);
            return null;
        }
        HelpdeskLogger.clear();
        destroyCallback();
        return null;
    }

    private void login(String userId) throws IOException {

        this.authenticationContext.setUserId(userId);
        this.authenticationContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        this.authenticationContext.setIssuer(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
        this.authenticationContext.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);

        exit();
    }

    @Begin
    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed"),
            @Error(exceptionClass = MobileException.class, messageId = "mobileCommunicationFailed") })
    public String requestOTP() throws MalformedURLException, MobileException, AttributeTypeNotFoundException, AttributeNotFoundException {

        LOG.debug("check mobile: " + this.mobile);
        try {
            this.encapDeviceService.checkMobile(this.mobile);
        } catch (SubjectNotFoundException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
            HelpdeskLogger.add("login: subject not found for " + this.mobile, LogLevelType.ERROR);
            return null;
        } catch (DeviceDisabledException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileDisabled");
            HelpdeskLogger.add("login: mobile " + this.mobile + " disabled", LogLevelType.ERROR);
            return null;
        }

        LOG.debug("request OTP: mobile=" + this.mobile);
        this.challengeId = this.encapDeviceService.requestOTP(this.mobile);
        LOG.debug("received challengeId: " + this.challengeId);
        return "success";
    }

    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed"),
            @Error(exceptionClass = MobileException.class, messageId = "mobileCommunicationFailed") })
    public String requestNewOTP() throws MalformedURLException, MobileException {

        LOG.debug("request new OTP: mobile=" + this.mobile);
        this.challengeId = this.encapDeviceService.requestOTP(this.mobile);
        LOG.debug("received new challengeId: " + this.challengeId);
        return "success";

    }

    public String cancel() throws IOException {

        exit();
        return null;
    }

    public String tryAnotherDevice() throws IOException {

        this.authenticationContext.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
        exit();
        return null;
    }

    private void exit() throws IOException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        String redirectUrl = "authenticationexit";
        LOG.debug("redirecting to: " + redirectUrl);
        externalContext.redirect(redirectUrl);
    }

    @PostConstruct
    public void init() {

        HelpdeskLogger.clear();
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy");
        this.mobileOTP = null;
        this.mobile = null;
    }

}
