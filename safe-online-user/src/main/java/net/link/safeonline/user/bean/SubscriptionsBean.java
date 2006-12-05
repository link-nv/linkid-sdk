package net.link.safeonline.user.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.user.Subscriptions;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("subscriptions")
@LocalBinding(jndiBinding = "SafeOnline/user/SubscriptionsBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class SubscriptionsBean implements Subscriptions {

	private static final Log LOG = LogFactory.getLog(SubscriptionsBean.class);

	@EJB
	private SubscriptionService subscriptionService;

	@EJB
	private ApplicationService applicationService;

	@In(create = true)
	FacesMessages facesMessages;

	@SuppressWarnings("unused")
	@DataModel
	private List<SubscriptionEntity> subscriptionList;

	@DataModelSelection("subscriptionList")
	@Out(value = "selectedSubscription", required = false, scope = ScopeType.SESSION)
	private SubscriptionEntity selectedSubscription;

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory("subscriptionList")
	public void subscriptionListFactory() {
		LOG.debug("subscription list factory");
		this.subscriptionList = this.subscriptionService.getSubscriptions();

	}

	@SuppressWarnings("unused")
	@DataModel
	private List<ApplicationEntity> applicationList;

	@DataModelSelection("applicationList")
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	private ApplicationEntity selectedApplication;

	@Factory("applicationList")
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.applicationList = this.applicationService.getApplications();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String viewSubscription() {
		LOG.debug("view subscription: "
				+ this.selectedSubscription.getApplication().getName());
		return "view-subscription";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String unsubscribe() {
		LOG.debug("unsubscribe from: "
				+ this.selectedSubscription.getApplication().getName());
		String applicationName = this.selectedSubscription.getApplication()
				.getName();
		try {
			this.subscriptionService.unsubscribe(applicationName);
		} catch (ApplicationNotFoundException e) {
			this.facesMessages.add("application not found");
			return null;
		} catch (SubscriptionNotFoundException e) {
			this.facesMessages.add("subscription not found");
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages
					.add("entity has no permission to unsubscribe from: "
							+ applicationName);
			return null;
		}
		subscriptionListFactory();
		return "unsubscribed";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String subscribe() {
		String applicationName = this.selectedApplication.getName();
		LOG.debug("subscribe on: " + applicationName);
		try {
			this.subscriptionService.subscribe(applicationName);
			/*
			 * TODO: The following catch sequence marks the need to create a
			 * base exception. That way we would only need a single catch
			 * statement, of course, what do we do about the i18n thing?
			 */
		} catch (ApplicationNotFoundException e) {
			this.facesMessages.add("application not found");
			return null;
		} catch (AlreadySubscribedException e) {
			this.facesMessages.add("already subscribed to " + applicationName);
			return null;
		} catch (PermissionDeniedException e) {
			this.facesMessages.add("permission denied to subscribe to "
					+ applicationName);
			return null;
		}
		subscriptionListFactory();
		return "subscribed";
	}

	public String viewApplication() {
		LOG.debug("view application: " + this.selectedApplication.getName());
		return "view-application";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
