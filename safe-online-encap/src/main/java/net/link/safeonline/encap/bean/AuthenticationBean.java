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
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.encap.Authentication;
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

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    private EncapDeviceService   encapDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService samlAuthorityService;

    private String               challengeId;

    private PhoneNumber          mobile;

    private String               mobileOTP;


    public String getMobileOTP() {

        return mobileOTP;
    }

    public void setMobileOTP(String mobileOTP) {

        this.mobileOTP = mobileOTP;
    }

    public PhoneNumber getMobile() {

        return mobile;
    }

    public void setMobile(PhoneNumber mobile) {

        this.mobile = mobile;
    }

    public String getChallengeId() {

        return challengeId;
    }

    public void setChallengeId(String challengeId) {

        this.challengeId = challengeId;
    }

    @End
    public String login()
            throws MobileAuthenticationException, IOException {

        LOG.debug("login: " + mobile);
        HelpdeskLogger.add("login: " + mobile, LogLevelType.INFO);
        try {
            String userId = encapDeviceService.authenticate(mobile.getNumber(), challengeId, mobileOTP);
            if (null == userId) {
                facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
                HelpdeskLogger.add("login failed: " + mobile, LogLevelType.ERROR);
                return null;
            }
            login(userId);
        } catch (SubjectNotFoundException e) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
            HelpdeskLogger.add("login: subject not found for " + mobile, LogLevelType.ERROR);
            return null;
        } catch (MalformedURLException e) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
            HelpdeskLogger.add("login: encap webservice not available", LogLevelType.ERROR);
            return null;
        } catch (MobileException e) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileCommunicationFailed");
            HelpdeskLogger.add("login: failed to contact encap webservice for " + mobile, LogLevelType.ERROR);
            return null;
        }
        HelpdeskLogger.clear();
        destroyCallback();
        return null;
    }

    private void login(String userId)
            throws IOException {

        authenticationContext.setUserId(userId);
        authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        authenticationContext.setIssuer(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
        authenticationContext.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);

        exit();
    }

    @Begin
    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed"),
            @Error(exceptionClass = MobileException.class, messageId = "mobileCommunicationFailed") })
    public String requestOTP()
            throws MalformedURLException, MobileException, AttributeTypeNotFoundException, AttributeNotFoundException {

        LOG.debug("check mobile: " + mobile);
        try {
            encapDeviceService.checkMobile(mobile.getNumber());
        } catch (SubjectNotFoundException e) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileNotRegistered");
            HelpdeskLogger.add("login: subject not found for " + mobile, LogLevelType.ERROR);
            return null;
        } catch (DeviceDisabledException e) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "mobileDisabled");
            HelpdeskLogger.add("login: mobile " + mobile + " disabled", LogLevelType.ERROR);
            return null;
        }

        LOG.debug("request OTP: mobile=" + mobile);
        challengeId = encapDeviceService.requestOTP(mobile.getNumber());
        LOG.debug("received challengeId: " + challengeId);
        return "success";
    }

    @ErrorHandling( { @Error(exceptionClass = MalformedURLException.class, messageId = "mobileCommunicationFailed"),
            @Error(exceptionClass = MobileException.class, messageId = "mobileCommunicationFailed") })
    public String requestNewOTP()
            throws MalformedURLException, MobileException {

        LOG.debug("request new OTP: mobile=" + mobile);
        challengeId = encapDeviceService.requestOTP(mobile.getNumber());
        LOG.debug("received new challengeId: " + challengeId);
        return "success";

    }

    public String cancel()
            throws IOException {

        exit();
        return null;
    }

    public String tryAnotherDevice()
            throws IOException {

        authenticationContext.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
        exit();
        return null;
    }

    private void exit()
            throws IOException {

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
        mobileOTP = null;
        mobile = null;
    }

}
