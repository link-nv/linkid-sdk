package net.link.safeonline.user.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.user.Password;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;
import org.jboss.security.SimplePrincipal;

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

		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		flushCredentialCache(login);

		this.sessionContext.set("password", this.newPassword);

		return "success";
	}

	// TODO: move to safe-online-j2ee-util
	private void flushCredentialCache(String login) {
		Principal user = new SimplePrincipal(login);
		ObjectName jaasMgr;
		try {
			jaasMgr = new ObjectName(
					"jboss.security:service=JaasSecurityManager");
		} catch (MalformedObjectNameException e) {
			String msg = "ObjectName error: " + e.getMessage();
			LOG.error(msg);
			throw new RuntimeException(msg, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("NPE: " + e.getMessage(), e);
		}
		Object[] params = { UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN,
				user };
		String[] signature = { String.class.getName(),
				Principal.class.getName() };
		MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(
				null).get(0);
		try {
			server.invoke(jaasMgr, "flushAuthenticationCache", params,
					signature);
		} catch (InstanceNotFoundException e) {
			String msg = "instance not found: " + e.getMessage();
			LOG.error(msg);
			throw new RuntimeException(msg, e);
		} catch (MBeanException e) {
			String msg = "mbean error: " + e.getMessage();
			LOG.error(msg);
			throw new RuntimeException(msg, e);
		} catch (ReflectionException e) {
			String msg = "reflection error: " + e.getMessage();
			LOG.error(msg);
			throw new RuntimeException(msg, e);
		}
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		this.oldPassword = null;
		this.newPassword = null;
	}
}
