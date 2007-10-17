/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;

@Local
public interface UsageAgreementManager {

	/**
	 * This commits the current draft usage agreement for the specified
	 * application to a new usage agreement version. And updates the appliation
	 * with this new version. This causes the user to have to agree with this
	 * updated version during his next authentication.
	 * 
	 * @param application
	 * @throws UsageAgreementNotFoundException
	 */
	void updateUsageAgreement(ApplicationEntity application)
			throws UsageAgreementNotFoundException;

	/**
	 * Updates/creates a draft usage agreement text for the specified
	 * application. If no draft usage agreement exists for this application,
	 * creates one.
	 * 
	 * @param application
	 * @param language
	 * @param text
	 */
	void updateUsageAgreementText(ApplicationEntity application,
			String language, String text);

	/**
	 * Set the usage agreement version of the specified application to the
	 * specified version. Draft versions are not taken into account.
	 * 
	 * @param application
	 * @param usageAgreementVersion
	 * @throws UsageAgreementNotFoundException
	 */
	void setUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion) throws UsageAgreementNotFoundException;
}
