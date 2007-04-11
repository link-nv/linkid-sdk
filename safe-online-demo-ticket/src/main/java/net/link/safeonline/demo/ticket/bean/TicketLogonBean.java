/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.demo.ticket.TicketLogon;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("ticketLogon")
@Scope(ScopeType.SESSION)
@CacheConfig(idleTimeoutSeconds = (5 + 1) * 60)
@LocalBinding(jndiBinding = "SafeOnlineTicketDemo/TicketLogonBean/local")
public class TicketLogonBean implements TicketLogon {

	public static final String APPLICATION_NAME = "safe-online-demo-ticket";

	@Logger
	private Log log;

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	public String login() {
		log.debug("login");

		return SafeOnlineLoginUtils.login(this.facesMessages, this.log,
				"overview.seam");
	}

	public String logout() {
		log.debug("logout");
		this.sessionContext.set("username", null);
		Seam.invalidateSession();
		return "logout-success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
