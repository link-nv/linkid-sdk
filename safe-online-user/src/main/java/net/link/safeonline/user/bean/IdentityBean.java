package net.link.safeonline.user.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.user.Identity;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Name;

@Stateless
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
		LOG.debug("get name: " + this.name);
		String login = getLogin();
		// TODO: we should propagate the login via the LoginContext instead
		// of via parameter
		// XXX: for now we don't cache here since all users share this bean.
		try {
			this.name = this.identityService.getName(login);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
		}
		return this.name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String save() {
		String login = getLogin();
		LOG.debug("save identity for " + login);
		try {
			this.identityService.saveName(login, this.name);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
		}
		return "success";
	}
}
