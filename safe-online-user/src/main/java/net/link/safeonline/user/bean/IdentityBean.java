/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.user.Identity;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;

@Stateful
@Name("identity")
@LocalBinding(jndiBinding = "SafeOnline/user/IdentityBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class IdentityBean implements Identity {

	private static final Log LOG = LogFactory.getLog(IdentityBean.class);

	@Resource
	private SessionContext context;

	private String name;

	@EJB
	private IdentityService identityService;

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getLogin() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		return login;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getName() {
		this.name = this.identityService.getName();
		LOG.debug("get name: " + this.name);
		return this.name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String save() {
		LOG.debug("save identity");
		this.identityService.saveName(this.name);
		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
