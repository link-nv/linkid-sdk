package net.link.safeonline.service.bean;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.AuthorizationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class AuthorizationServiceBean implements AuthorizationService {

	private static final Log LOG = LogFactory
			.getLog(AuthorizationServiceBean.class);

	@EJB
	private SubjectDAO entityDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	public Set<String> getRoles(String login) {
		Set<String> roles = new HashSet<String>();

		LOG.debug("get roles for login: " + login);

		SubjectEntity subject;
		try {
			subject = this.entityDAO.getSubject(login);
		} catch (SubjectNotFoundException e) {
			LOG.error("entity not found: " + login);
			return roles;
		}

		ApplicationEntity safeOnlineUserApplication;
		try {
			safeOnlineUserApplication = this.applicationDAO
					.getApplication(UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME);
		} catch (ApplicationNotFoundException e) {
			LOG
					.error("application not found: "
							+ UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME);
			return roles;
		}

		/*
		 * For now we base the authorization on made subscriptions. Of course,
		 * later on we could let this decision depend on explicit ACL.
		 */
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, safeOnlineUserApplication);
		if (null != subscription) {
			roles.add(SafeOnlineConstants.USER_ROLE);
		}

		return roles;
	}
}
