/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;

@Local
public interface UsageAgreementDAO {

	UsageAgreementEntity addUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion);

	UsageAgreementEntity getUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion) throws UsageAgreementNotFoundException;

	List<UsageAgreementEntity> listUsageAgreements(ApplicationEntity application);

	/**
	 * Removes a usage agreement. This will also remove the usage agreement
	 * texts related.
	 * 
	 * @param usageAgreement
	 */
	void removeusageAgreement(UsageAgreementEntity usageAgreement);

	UsageAgreementTextEntity addUsageAgreementText(
			UsageAgreementEntity usageAgreement, String text, String language);

	void removeUsageAgreementText(UsageAgreementTextEntity usageAgreementText);

}
