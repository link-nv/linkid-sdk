package net.link.safeonline.user.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.user.Identity;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("identity")
@LocalBinding(jndiBinding = "SafeOnline/user/IdentityBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class IdentityBean implements Identity {

	@Resource
	private SessionContext context;

	// @RolesAllowed(UserConstants.USER_ROLE)
	public String getLogin() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		return login;
	}
}
