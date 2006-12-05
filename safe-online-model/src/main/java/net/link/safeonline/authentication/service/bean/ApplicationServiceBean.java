package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;

/**
 * Implementation of application service interface. This component does not live
 * within the SafeOnline core security domain.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class ApplicationServiceBean implements ApplicationService {

	@EJB
	private ApplicationDAO applicationDAO;

	public List<ApplicationEntity> getApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.getApplications();
		return applications;
	}
}
