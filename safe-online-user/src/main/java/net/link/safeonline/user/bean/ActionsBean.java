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
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.user.Actions;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("actions")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "ActionsBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class ActionsBean implements Actions {

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private AccountService accountService;

	@Logger
	private Log log;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String removeAccount() {
		this.log.debug("remove account");
		try {
			this.accountService.removeAccount();
		} catch (SubscriptionNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubscriptionNotFound");
			return null;
		} catch (MessageHandlerNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMessage");
			return null;
		}
		this.sessionContext.set("login-processing", null);
		this.sessionContext.set("username", null);
		Seam.invalidateSession();
		return "logout-success";
	}
}
