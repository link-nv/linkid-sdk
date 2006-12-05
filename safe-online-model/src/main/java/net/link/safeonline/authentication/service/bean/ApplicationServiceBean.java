package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of application service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class ApplicationServiceBean implements ApplicationService {

	private static final Log LOG = LogFactory
			.getLog(ApplicationServiceBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@PermitAll
	public List<ApplicationEntity> getApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}

	@RolesAllowed(SafeOnlineConstants.OPERATOR_ROLE)
	public void addApplication(String name, String description) {
		LOG.debug("add application: " + name);
		ApplicationEntity newApplication = new ApplicationEntity(name);
		newApplication.setDescription(description);
		this.applicationDAO.addApplication(newApplication);
	}
}
