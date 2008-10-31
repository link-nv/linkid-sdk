/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.user.Actions;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.web.Session;


@Stateful
@Name("actions")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "ActionsBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class ActionsBean implements Actions {

    @In
    Context                sessionContext;

    @In(create = true)
    FacesMessages          facesMessages;

    @EJB
    private AccountService accountService;

    @Logger
    private Log            log;


    @Remove
    @Destroy
    public void destroyCallback() {

    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String removeAccount() throws SubscriptionNotFoundException, MessageHandlerNotFoundException {

        this.log.debug("remove account");

        this.accountService.removeAccount();

        removeLoginCookie();
        this.sessionContext.set(LoginManager.USERID_SESSION_ATTRIBUTE, null);
        Session.instance().invalidate();
        return "success";
    }

    private void removeLoginCookie() {

        this.log.debug("remove login cookie");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "");
        loginCookie.setPath(facesContext.getExternalContext().getRequestContextPath());
        loginCookie.setMaxAge(0);
        response.addCookie(loginCookie);
    }

}
