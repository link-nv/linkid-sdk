package net.link.safeonline.model.bean;

import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.model.Applications;

@Stateless
public class ApplicationsBean implements Applications {

	private static final Log LOG = LogFactory.getLog(ApplicationsBean.class);

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private ApplicationIdentityDAO applicationIdentityDAO;

	@EJB
	private UsageAgreementDAO usageAgreememtDAO;

	public ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException {
		return this.applicationDAO.getApplication(applicationName);
	}

	public List<ApplicationEntity> listApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.listApplications();
		return applications;
	}

	public List<ApplicationEntity> listUserApplications() {
		List<ApplicationEntity> applications = this.applicationDAO
				.listUserApplications();
		return applications;
	}

	public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(
			ApplicationEntity application)
			throws ApplicationIdentityNotFoundException {

		LOG.debug("get current application identity: " + application.getName());

		long currentIdentityVersion = application
				.getCurrentApplicationIdentity();
		ApplicationIdentityEntity applicationIdentity = this.applicationIdentityDAO
				.getApplicationIdentity(application, currentIdentityVersion);
		Set<ApplicationIdentityAttributeEntity> attributes = applicationIdentity
				.getAttributes();
		for (ApplicationIdentityAttributeEntity attribute : attributes) {
			LOG.debug("attribute: " + attribute);
		}
		return attributes;
	}

	public UsageAgreementEntity getCurrentUsageAgreement(
			ApplicationEntity application) {
		LOG.debug("get current application usage agreement: "
				+ application.getName());

		long currentUsageAgreementVersion = application
				.getCurrentApplicationUsageAgreement();
		UsageAgreementEntity usageAgreement;
		try {
			usageAgreement = this.usageAgreememtDAO.getUsageAgreement(
					application, currentUsageAgreementVersion);
		} catch (UsageAgreementNotFoundException e) {
			LOG.debug("empty usage agreement for appliaction: "
					+ application.getName());
			return null;
		}
		return usageAgreement;
	}
}
