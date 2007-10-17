package net.link.safeonline.model;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.UsageAgreementEntity;

@Local
public interface Applications {

	public ApplicationEntity getApplication(String applicationName)
			throws ApplicationNotFoundException;

	public List<ApplicationEntity> listApplications();

	public List<ApplicationEntity> listUserApplications();

	public Set<ApplicationIdentityAttributeEntity> getCurrentApplicationIdentity(
			ApplicationEntity application)
			throws ApplicationIdentityNotFoundException;

	public UsageAgreementEntity getCurrentUsageAgreement(
			ApplicationEntity application);
}
