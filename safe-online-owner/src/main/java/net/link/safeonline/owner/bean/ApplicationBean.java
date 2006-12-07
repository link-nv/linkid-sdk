package net.link.safeonline.owner.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.owner.Application;
import net.link.safeonline.owner.OwnerConstants;

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
@Name("ownerApplication")
@LocalBinding(jndiBinding = "SafeOnline/owner/ApplicationBean/local")
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
public class ApplicationBean implements Application {

	private static final Log LOG = LogFactory.getLog(ApplicationBean.class);

	@EJB
	private ApplicationService applicationService;

	@EJB
	private SubscriptionService subscriptionService;

	@In(create = true)
	FacesMessages facesMessages;

	@SuppressWarnings("unused")
	@Out
	private long numberOfSubscriptions;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@SuppressWarnings("unused")
	@DataModel
	private List<ApplicationEntity> ownerApplicationList;

	@DataModelSelection("ownerApplicationList")
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	private ApplicationEntity selectedApplication;

	@In(value = "selectedApplication", required = false)
	private ApplicationEntity editApplication;

	@Factory("ownerApplicationList")
	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.ownerApplicationList = this.applicationService.getApplications();
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String view() {
		String applicationName = this.selectedApplication.getName();
		LOG.debug("view: " + applicationName);
		try {
			this.numberOfSubscriptions = this.subscriptionService
					.getNumberOfSubscriptions(applicationName);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "view-application";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String edit() {
		LOG.debug("edit: " + this.selectedApplication.getName());
		return "edit-application";
	}

	@RolesAllowed(OwnerConstants.OWNER_ROLE)
	public String save() {
		String applicationName = this.editApplication.getName();
		String applicationDescription = this.editApplication.getDescription();
		LOG.debug("save: " + applicationName);
		LOG.debug("description: " + applicationDescription);
		try {
			this.applicationService.setApplicationDescription(applicationName,
					applicationDescription);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "saved";
	}
}
