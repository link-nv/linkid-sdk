package net.link.safeonline.user.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.user.Password;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

@Stateless
@Name("passwordBean")
@LocalBinding(jndiBinding = "SafeOnline/user/PasswordBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class PasswordBean implements Password {

	private static final Log LOG = LogFactory.getLog(PasswordBean.class);

	private String oldPassword;

	private String newPassword;

	@EJB
	private CredentialService credentialService;

	@Resource
	private SessionContext context;

	@In(create = true)
	FacesMessages facesMessages;

	public String getNewPassword() {
		return "";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return "";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String change() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		try {
			this.credentialService.changePassword(login, this.oldPassword,
					this.newPassword);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
		} catch (PermissionDeniedException e) {
			String msg = "old password not correct";
			LOG.debug(msg);
			this.facesMessages.add("oldpassword", msg);
			return null;
		}
		return "success";
	}
}
