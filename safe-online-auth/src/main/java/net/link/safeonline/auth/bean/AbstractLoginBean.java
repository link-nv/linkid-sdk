/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.service.SubjectService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


/**
 * Abstract login bean. Encapsulates the common code for a Seam backing bean to login a given user.
 * 
 * @author fcorneli
 * 
 */
public class AbstractLoginBean {

    @Logger
    private Log          log;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    SubjectService       subjectService;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    DeviceDAO            deviceDAO;

    @Out(value = LoginManager.USERID_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
    @In(value = LoginManager.USERID_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
    protected String     userId;

    @SuppressWarnings("unused")
    @Out(value = LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private DeviceEntity authenticationDevice;

    @In(create = true)
    FacesMessages        facesMessages;


    protected void clearUserId() {

        userId = null;
    }

    /**
     * Login the given user.
     */
    protected void login(String inputUsername, String inputAuthenticationDevice, Cookie ssoCookie) {

        log.debug("login using: " + inputUsername + " via device: " + inputAuthenticationDevice);
        userId = subjectService.findSubjectFromUserName(inputUsername).getUserId();
        relogin(inputAuthenticationDevice, ssoCookie);
    }

    /**
     * Re-login the current user. This will trigger the device restriction check again.
     * 
     * @param inputAuthenticationDevice
     */
    protected void relogin(String inputAuthenticationDevice, Cookie ssoCookie) {

        authenticationDevice = deviceDAO.findDevice(inputAuthenticationDevice);
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        if (null != ssoCookie) {
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            response.addCookie(ssoCookie);
        }

        String redirectUrl = "../login";
        log.debug("redirecting to: " + redirectUrl);
        try {
            externalContext.redirect(redirectUrl);
        } catch (IOException e) {
            log.debug("IO error: " + e.getMessage());
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorIO");
            return;
        }
    }
}
