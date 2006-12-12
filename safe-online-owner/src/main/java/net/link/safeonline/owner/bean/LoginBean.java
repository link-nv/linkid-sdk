/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.owner.Login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

/**
 * This Seam components implements the login interface. This component cannot
 * live within the security domain of the SafeOnline user web application since
 * the user still has to logon onto the system.
 * 
 * @author fcorneli
 * 
 * XXX: Seam component name lookup clashes between two WARs. Because of this we
 * have to prefix the user and operator Seam components' names.
 * 
 * Because of http session timeout being set to 5 minutes in web.xml we have to
 * make sure that the lifecycle of the login bean that has session scope is
 * longer than 5 minutes. Thus we take 5 + 1 minutes.
 * 
 */
@Stateful
@Name("ownerLogin")
@Scope(ScopeType.SESSION)
@CacheConfig(idleTimeoutSeconds = (5 + 1) * 60)
@LocalBinding(jndiBinding = "SafeOnline/owner/LoginBean/local")
public class LoginBean implements Login {

	private static final Log LOG = LogFactory.getLog(LoginBean.class);

	@In
	Context sessionContext;

	private String username;

	private String password;

	@EJB
	private AuthenticationService authenticationService;

	@In(create = true)
	FacesMessages facesMessages;

	public LoginBean() {
		LOG.debug("constructor: " + this);
	}

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct: " + this);
	}

	@PreDestroy
	public void preDestroyCallback() {
		LOG.debug("pre destroy: " + this);
	}

	@PostActivate
	public void postActivateCallback() {
		LOG.debug("post activate: " + this);
	}

	@PrePassivate
	public void prePassivateCallback() {
		LOG.debug("pre passivate: " + this);
	}

	public String getPassword() {
		LOG.debug("get password");
		return "";
	}

	public String getUsername() {
		LOG.debug("get username");
		return this.username;
	}

	public String login() {
		String applicationName = "safe-online-owner";
		LOG.debug("login with username: " + this.username + " into "
				+ applicationName);
		boolean authenticated = this.authenticationService.authenticate(
				applicationName, this.username, new String(this.password));
		if (!authenticated) {
			this.facesMessages.add("username", "login failed");
			Seam.invalidateSession();
			return null;
		}

		this.sessionContext.set("username", this.username);
		this.sessionContext.set("password", this.password);

		return "login-success";
	}

	public void setPassword(String password) {
		LOG.debug("set password");
		this.password = password;
	}

	public void setUsername(String username) {
		LOG.debug("set username");
		this.username = username;
	}

	public String logout() {
		LOG.debug("logout");
		this.sessionContext.set("username", null);
		this.sessionContext.set("password", null);
		Seam.invalidateSession();
		return "logout-success";
	}

	public String getLoggedInUsername() {
		LOG.debug("get logged in username");
		String username = (String) this.sessionContext.get("username");
		return username;
	}

	public boolean isLoggedIn() {
		LOG.debug("is logged in");
		String username = (String) this.sessionContext.get("username");
		return (null != username);
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy: " + this);
		this.username = null;
		this.password = null;
	}
}
