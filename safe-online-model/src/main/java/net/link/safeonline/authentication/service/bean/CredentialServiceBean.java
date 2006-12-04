package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

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
		SecurityManagerUtils.flushCredentialCache(login,
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}
}
