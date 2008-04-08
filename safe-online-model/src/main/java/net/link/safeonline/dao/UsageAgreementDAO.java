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
import net.link.safeonline.authentication.exception.UsageAgreementTextNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;

@Local
public interface UsageAgreementDAO {

	UsageAgreementEntity addUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion);

	UsageAgreementEntity getUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion);

	UsageAgreementEntity findUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion) throws UsageAgreementNotFoundException;

	List<UsageAgreementEntity> listUsageAgreements(ApplicationEntity application);

	void removeUsageAgreements(ApplicationEntity application);

	/**
	 * Removes a usage agreement. This will also remove the usage agreement
	 * texts related.
	 * 
	 * @param usageAgreement
	 */
	void removeUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion);

	List<UsageAgreementTextEntity> listUsageAgreementTexts(
			ApplicationEntity application, Long usageAgreementVersion);

	UsageAgreementTextEntity addUsageAgreementText(
			UsageAgreementEntity usageAgreement, String text, String language);

	void removeUsageAgreementText(UsageAgreementTextEntity usageAgreementText);

	UsageAgreementTextEntity getUsageAgreementText(
			UsageAgreementEntity usageAgreement, String language);

	UsageAgreementTextEntity findUsageAgreementText(
			UsageAgreementEntity usageAgreement, String language)
			throws UsageAgreementTextNotFoundException;

	GlobalUsageAgreementEntity addGlobalUsageAgreement(
			Long draftGlobalUsageAgreementVersion);

	GlobalUsageAgreementEntity getGlobalUsageAgreement();

	GlobalUsageAgreementEntity getGlobalUsageAgreement(
			Long draftGlobalUsageAgreementVersion);

	UsageAgreementTextEntity addGlobalUsageAgreementText(
			GlobalUsageAgreementEntity draftUsageAgreement, String text,
			String language);

	void removeGlobalUsageAgreement(Long usageAgreementVersion);

	UsageAgreementTextEntity getGlobalUsageAgreementText(
			GlobalUsageAgreementEntity usageAgreement, String language);

	void removeGlobalUsageAgreementText(
			UsageAgreementTextEntity usageAgreementText);
}
