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

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.user.Password;
import net.link.safeonline.user.UserConstants;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

@Stateful
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

	@In
	Context sessionContext;

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
		try {
			this.credentialService.changePassword(this.oldPassword,
					this.newPassword);
		} catch (PermissionDeniedException e) {
			String msg = "old password not correct";
			LOG.debug(msg);
			this.facesMessages.add("oldpassword", msg);
			return null;
		}

		this.sessionContext.set("password", this.newPassword);

		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy callback");
		/*
		 * We postpone the flushing of the credential cache of the principal
		 * until destroy time. Else Seam? will login with the old credentials
		 * while the security domain is already loaded with the new credentials.
		 * This could cause an authentication error, leaving the stateful
		 * password bean in an inconsistent state.
		 */
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		SecurityManagerUtils.flushCredentialCache(login,
				UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN);
		this.oldPassword = null;
		this.newPassword = null;
	}
}
