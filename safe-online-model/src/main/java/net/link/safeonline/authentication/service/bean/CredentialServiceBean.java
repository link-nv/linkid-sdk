package net.link.safeonline.authentication.service.bean;

import java.security.Principal;

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
import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.entity.EntityEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SimplePrincipal;

@Stateless
public class CredentialServiceBean implements CredentialService {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private EntityDAO entityDAO;

	public void changePassword(String login, String oldPassword,
			String newPassword) throws EntityNotFoundException,
			PermissionDeniedException {
		LOG.debug("change password of: " + login);
		EntityEntity entity = this.entityDAO.getEntity(login);
		if (!entity.getPassword().equals(oldPassword)) {
			throw new PermissionDeniedException();
		}
		entity.setPassword(newPassword);

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
