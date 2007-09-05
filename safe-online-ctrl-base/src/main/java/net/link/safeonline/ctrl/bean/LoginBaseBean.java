/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;

import net.link.safeonline.ctrl.LoginBase;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

public class LoginBaseBean implements LoginBase {

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	@Logger
	private Log log;

	@PostConstruct
	public void postConstructCallback() {
		log.debug("post construct: #0", this);
	}

	@PreDestroy
	public void preDestroyCallback() {
		log.debug("pre destroy: #0", this);
	}

	@PostActivate
	public void postActivateCallback() {
		log.debug("post activate: #0", this);
	}

	@PrePassivate
	public void prePassivateCallback() {
		log.debug("pre passivate: #0", this);
	}

	public String login() {
		/*
		 * The 'login-processing' session attribute is used by the timeout
		 * filter to help in detecting an application level session timeout.
		 */
		this.sessionContext.set("login-processing", "true");
		return SafeOnlineLoginUtils.login(this.facesMessages, this.log,
				"overview.seam");
	}

	public String logout() {
		log.debug("logout");
		this.sessionContext.set("login-processing", null);
		this.sessionContext.set("username", null);
		Seam.invalidateSession();
		return "logout-success";
	}

	public String getLoggedInUsername() {
		log.debug("get logged in username");
		String username = (String) this.sessionContext.get("username");
		return username;
	}

	public boolean isLoggedIn() {
		log.debug("is logged in");
		String username = (String) this.sessionContext.get("username");
		return (null != username);
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
