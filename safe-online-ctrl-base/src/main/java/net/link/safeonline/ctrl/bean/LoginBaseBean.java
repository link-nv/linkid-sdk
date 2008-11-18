/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.ctrl.LoginBase;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;
import net.link.safeonline.service.SubjectService;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.log.Log;


public class LoginBaseBean implements LoginBase {

    @In
    Context                sessionContext;

    @In(create = true)
    FacesMessages          facesMessages;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService subjectService;

    @In(create = true)
    LocaleSelector         localeSelector;

    @Logger
    private Log            log;


    @PostConstruct
    public void postConstructCallback() {

        this.log.debug("post construct: #0", this);
    }

    @PreDestroy
    public void preDestroyCallback() {

        this.log.debug("pre destroy: #0", this);
    }

    @PostActivate
    public void postActivateCallback() {

        this.log.debug("post activate: #0", this);
    }

    @PrePassivate
    public void prePassivateCallback() {

        this.log.debug("pre passivate: #0", this);
    }

    public String login() {

        /*
         * The login cookie is used to help in detecting an application level session timeout.
         */
        addLoginCookie();
        return SafeOnlineLoginUtils.login("overview.seam", this.localeSelector.getLocale(), null, false);
    }

    public String logout() {

        this.log.debug("logout");
        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        removeLoginCookie();
        SafeOnlineLoginUtils.logout(userId, "main.seam");
        return "success";
    }

    protected void addLoginCookie() {

        this.log.debug("add login cookie");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "true");
        loginCookie.setPath(facesContext.getExternalContext().getRequestContextPath());
        response.addCookie(loginCookie);
    }

    protected void removeLoginCookie() {

        this.log.debug("remove login cookie");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "");
        loginCookie.setPath(facesContext.getExternalContext().getRequestContextPath());
        loginCookie.setMaxAge(0);
        response.addCookie(loginCookie);
    }

    public String getLoggedInUsername() {

        this.log.debug("get logged in username");
        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        String username = this.subjectService.getSubjectLogin(userId);
        return username;
    }

    public boolean isLoggedIn() {

        this.log.debug("is logged in");
        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        return null != userId;
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        this.log.debug("destroy: #0", this);
    }
}
