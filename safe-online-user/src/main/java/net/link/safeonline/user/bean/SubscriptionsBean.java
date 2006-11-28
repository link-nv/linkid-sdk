package net.link.safeonline.user.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.EntityNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.user.Subscriptions;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;

@Stateless
@Name("subscriptions")
@LocalBinding(jndiBinding = "SafeOnline/user/SubscriptionsBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class SubscriptionsBean implements Subscriptions {

	private static final Log LOG = LogFactory.getLog(SubscriptionsBean.class);

	@EJB
	private SubscriptionService subscriptionService;

	@Resource
	private SessionContext context;

	@DataModel
	private List<SubscriptionEntity> subscriptionList;

	@DataModelSelection
	@Out(value = "selectedSubscription", required = false, scope = ScopeType.SESSION)
	private SubscriptionEntity selectedSubscription;

	// TODO: @RolesAllowed(UserConstants.USER_ROLE)
	@Factory("subscriptionList")
	public void subscriptionListFactory() {
		LOG.debug("subscription list factory");
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		try {
			this.subscriptionList = this.subscriptionService
					.getSubscriptions(login);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
			/*
			 * XXX: this should not be possible since we're already in the
			 * security domain
			 */
		}
	}

	// TODO: @RolesAllowed(UserConstants.USER_ROLE)
	public String view() {
		LOG.debug("view: "
				+ this.selectedSubscription.getApplication().getName());
		return "view";
	}

	// TODO: @RolesAllowed(UserConstants.USER_ROLE)
	public String unsubscribe() {
		LOG.debug("unsubscribe from: "
				+ this.selectedSubscription.getApplication().getName());
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		String applicationName = this.selectedSubscription.getApplication()
				.getName();
		// XXX: unsubscribe should work directly with application
		try {
			this.subscriptionService.unsubscribe(login, applicationName);
		} catch (ApplicationNotFoundException e) {
			throw new RuntimeException("application not found");
		} catch (EntityNotFoundException e) {
			throw new RuntimeException("entity not found");
		} catch (SubscriptionNotFoundException e) {
			throw new RuntimeException("subscription not found");
		}
		subscriptionListFactory();
		return "unsubscribed";
	}
}
