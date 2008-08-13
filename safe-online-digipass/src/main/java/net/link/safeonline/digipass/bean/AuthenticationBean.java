/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.digipass.bean;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.digipass.Authentication;
import net.link.safeonline.digipass.DigipassConstants;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("digipassAuthentication")
@LocalBinding(jndiBinding = DigipassConstants.JNDI_PREFIX + "AuthenticationBean/local")
@Interceptors(ErrorMessageInterceptor.class)
public class AuthenticationBean implements Authentication {

    private static final Log      LOG = LogFactory.getLog(AuthenticationBean.class);

    @In(create = true)
    FacesMessages                 facesMessages;

    @In(value = AuthenticationContext.AUTHENTICATION_CONTEXT)
    AuthenticationContext         authenticationContext;

    @EJB
    private DigipassDeviceService digipassDeviceService;

    @EJB
    private SamlAuthorityService  samlAuthorityService;

    private String                loginName;

    private String                token;


    public String getLoginName() {

        return this.loginName;
    }

    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }

    public String getToken() {

        return this.token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public String login() throws IOException {

        LOG.debug("login: " + this.loginName);
        HelpdeskLogger.add("login: " + this.loginName, LogLevelType.INFO);

        try {
            String deviceUserId = this.digipassDeviceService.authenticate(this.loginName, this.token);
            if (null == deviceUserId) {
                this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
                HelpdeskLogger.add("login failed: " + this.loginName, LogLevelType.ERROR);
                return null;
            }
            login(deviceUserId);
        } catch (SubjectNotFoundException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "digipassNotRegistered");
            HelpdeskLogger.add("login: subject not found for " + this.loginName, LogLevelType.ERROR);
            return null;
        } catch (PermissionDeniedException e) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "digipassAuthenticationFailed");
            HelpdeskLogger.add("Failed to contact OLAS to retrieve device mapping for " + this.loginName,
                    LogLevelType.ERROR);
            return null;
        }
        HelpdeskLogger.clear();
        destroyCallback();
        return null;
    }

    private void login(String deviceUserId) throws IOException {

        this.authenticationContext.setUserId(deviceUserId);
        this.authenticationContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        this.authenticationContext.setIssuer(net.link.safeonline.model.digipass.DigipassConstants.DIGIPASS_DEVICE_ID);
        this.authenticationContext
                .setUsedDevice(net.link.safeonline.model.digipass.DigipassConstants.DIGIPASS_DEVICE_ID);

        exit();
    }

    public String cancel() throws IOException {

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

        LOG.debug("remove");
        this.loginName = null;
        this.token = null;
    }

    public String tryAnotherDevice() throws IOException {

        this.authenticationContext
                .setUsedDevice(net.link.safeonline.model.digipass.DigipassConstants.DIGIPASS_DEVICE_ID);
        exit();
        return null;
    }
}
