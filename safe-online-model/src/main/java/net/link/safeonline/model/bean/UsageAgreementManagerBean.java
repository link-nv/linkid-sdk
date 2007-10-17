/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.model.UsageAgreementManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class UsageAgreementManagerBean implements UsageAgreementManager {

	private final static Log LOG = LogFactory
			.getLog(UsageAgreementManagerBean.class);

	@EJB
	private UsageAgreementDAO usageAgreementDAO;

	public void setUsageAgreement(ApplicationEntity application,
			Long usageAgreementVersion) throws UsageAgreementNotFoundException {
		LOG.debug("set usage agreement for application: "
				+ application.getName() + " to version: "
				+ usageAgreementVersion);
		this.usageAgreementDAO.getUsageAgreement(application,
				usageAgreementVersion);
		application.setCurrentApplicationUsageAgreement(usageAgreementVersion);
	}

	public void updateUsageAgreement(ApplicationEntity application)
			throws UsageAgreementNotFoundException {
		LOG.debug("update usage agreement for application: "
				+ application.getName());
		UsageAgreementEntity draftUsageAgreement = this.usageAgreementDAO
				.getUsageAgreement(application,
						UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
		long newUsageAgreementVersion;
		if (application.getCurrentApplicationUsageAgreement() == UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION)
			newUsageAgreementVersion = UsageAgreementPK.INITIAL_USAGE_AGREEMENT_VERSION;
		else
			newUsageAgreementVersion = application
					.getCurrentApplicationUsageAgreement() + 1;
		draftUsageAgreement.releaseNewVersion(newUsageAgreementVersion);
		application
				.setCurrentApplicationUsageAgreement(newUsageAgreementVersion);
	}

	public void updateUsageAgreementText(ApplicationEntity application,
			String language, String text) {
		LOG.debug("update usage agreement text for application: "
				+ application.getName() + ", language: " + language);
		UsageAgreementEntity draftUsageAgreement;
		try {
			draftUsageAgreement = this.usageAgreementDAO
					.getUsageAgreement(application,
							UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
		} catch (UsageAgreementNotFoundException e) {
			LOG.debug("create draft usage agreement for application: "
					+ application.getName());
			draftUsageAgreement = this.usageAgreementDAO
					.addUsageAgreement(application,
							UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
		}

		UsageAgreementTextEntity draftUsageAgreementText = draftUsageAgreement
				.getUsageAgreementText(language);
		if (null == draftUsageAgreementText) {
			draftUsageAgreementText = this.usageAgreementDAO
					.addUsageAgreementText(draftUsageAgreement, text, language);
		} else {
			draftUsageAgreementText.setText(text);
		}
	}
}
