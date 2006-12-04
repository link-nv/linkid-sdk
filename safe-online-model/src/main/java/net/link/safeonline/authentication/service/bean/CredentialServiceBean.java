package net.link.safeonline.authentication.service.bean;

import java.security.Principal;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.security.SimplePrincipal;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class CredentialServiceBean implements CredentialService {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@RolesAllowed(SafeOnlineConstants.USER_ROLE)
	public void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException {
		LOG.debug("change password");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		if (!subject.getPassword().equals(oldPassword)) {
			throw new PermissionDeniedException();
		}
		subject.setPassword(newPassword);

		String login = subject.getLogin();
		flushCredentialCache(login);
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
		Object[] params = { SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN,
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
}
