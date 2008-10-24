/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;


/**
 * Abstract exit bean. Encapsulates the common code for a Seam backing bean to return the optional application URL.
 * 
 * @author wvdhaute
 * 
 */
public class AbstractExitBean {

    @Logger
    private Log                      log;

    @EJB
    private PublicApplicationService publicApplicationService;


    /**
     * 
     * Returns the application's URL (if specified) we are authenticating for. Used to return to there if the authentication failed due to
     * events like timeout, missing attributes ( unavailable due to missing plugin, bad configuration ), ...
     */
    protected String findApplicationUrl() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            Cookie applicationCookie = (Cookie) facesContext.getExternalContext().getRequestCookieMap().get(
                    SafeOnlineCookies.APPLICATION_COOKIE);
            PublicApplication application = this.publicApplicationService.findPublicApplication(applicationCookie.getValue());
            if (null != application) {
                if (null != application.getUrl()) {
                    this.log.debug("found url: " + application.getUrl().toString());
                    return application.getUrl().toString() + "?authenticationTimeout=true";
                }
            }
            return null;
        } finally {
            this.log.debug("removing entry and timeout cookie");
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            removeCookie(SafeOnlineCookies.TIMEOUT_COOKIE, response);
            removeCookie(SafeOnlineCookies.ENTRY_COOKIE, response);
            removeCookie(SafeOnlineCookies.APPLICATION_COOKIE, response);
        }
    }

    private void removeCookie(String name, HttpServletResponse response) {

        this.log.debug("remove cookie: " + name);
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
