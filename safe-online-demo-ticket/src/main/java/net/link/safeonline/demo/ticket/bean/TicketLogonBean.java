/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import javax.ejb.Stateful;

import net.link.safeonline.demo.ticket.TicketLogon;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;


@Stateful
@Name("ticketLogon")
@Scope(ScopeType.SESSION)
@CacheConfig(idleTimeoutSeconds = (5 + 1) * 60)
@LocalBinding(jndiBinding = TicketLogon.JNDI_BINDING)
public class TicketLogonBean extends AbstractTicketDataClientBean implements TicketLogon {

    public static final String APPLICATION_NAME = "safe-online-demo-ticket";

    @Logger
    private Log                log;

    @In
    Context                    sessionContext;


    public String login() {

        this.log.debug("login");

        return SafeOnlineLoginUtils.login("overview.seam");
    }

    public String logout() {

        this.log.debug("logout");
        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        SafeOnlineLoginUtils.logout(userId, "main.seam");
        return "success";
    }

    public String getUsername() {

        String userId = (String) this.sessionContext.get(LoginManager.USERID_SESSION_ATTRIBUTE);
        return getUsername(userId);
    }
}
