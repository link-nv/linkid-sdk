package net.link.safeonline.user.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.user.History;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("history")
@LocalBinding(jndiBinding = "SafeOnline/user/HistoryBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class HistoryBean implements History {

	@EJB
	private IdentityService identityService;

	@Resource
	private SessionContext context;

	public List<HistoryEntity> getList() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		List<HistoryEntity> result;
		try {
			result = this.identityService.getHistory(login);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
		}
		return result;
	}
}
