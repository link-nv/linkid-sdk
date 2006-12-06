package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.oper.Application;
import net.link.safeonline.oper.OperatorConstants;

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
@Name("application")
@LocalBinding(jndiBinding = "SafeOnline/oper/ApplicationBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationBean implements Application {

	private static final Log LOG = LogFactory.getLog(ApplicationBean.class);

	@EJB
	private ApplicationService applicationService;

	@EJB
	private SubscriptionService subscriptionService;

	private String name;

	private String description;

	@SuppressWarnings("unused")
	@Out
	private long numberOfSubscriptions;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.name = null;
		this.description = null;
	}

	@SuppressWarnings("unused")
	@DataModel
	private List<ApplicationEntity> operApplicationList;

	@DataModelSelection("operApplicationList")
	@Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
	private ApplicationEntity selectedApplication;

	@Factory("operApplicationList")
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void applicationListFactory() {
		LOG.debug("application list factory");
		this.operApplicationList = this.applicationService.getApplications();
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
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

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String add() {
		LOG.debug("add application: " + this.name);
		this.applicationService.addApplication(this.name, this.description);
		return "success";
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeApplication() {
		/*
		 * http://jira.jboss.com/jira/browse/EJBTHREE-786
		 */
		String applicationName = this.selectedApplication.getName();
		LOG.debug("remove application: " + applicationName);
		try {
			this.applicationService.removeApplication(applicationName);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (PermissionDeniedException e) {
			String msg = "permission denied to remove: " + applicationName;
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		applicationListFactory();
		return "success";
	}
}
