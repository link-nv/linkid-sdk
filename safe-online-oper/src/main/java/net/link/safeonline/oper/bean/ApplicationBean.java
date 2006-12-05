package net.link.safeonline.oper.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.service.ApplicationService;
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
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;

@Stateful
@Name("application")
@LocalBinding(jndiBinding = "SafeOnline/oper/ApplicationBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
public class ApplicationBean implements Application {

	private static final Log LOG = LogFactory.getLog(ApplicationBean.class);

	@EJB
	private ApplicationService applicationService;

	@Remove
	@Destroy
	public void destroyCallback() {
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
		LOG.debug("view: " + this.selectedApplication.getName());
		return null;
	}
}
