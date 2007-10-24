/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.usage;

import javax.ejb.Local;

import net.link.safeonline.entity.GlobalUsageAgreementEntity;

@Local
public interface GlobalUsageAgreement {
	/*
	 * Accessors
	 */
	void setLanguage(String language);

	String getLanguage();

	GlobalUsageAgreementEntity getCurrentUsageAgreement();

	GlobalUsageAgreementEntity getDraftUsageAgreement();

	String getUsageAgreementVersion();

	/*
	 * Actions
	 */
	String releaseDraft();

	String removeDraft();

	String addText();

	String saveText();

	String viewCurrentText();

	String viewDraftText();

	String editDraftText();

	String removeDraftText();

	String editCurrentText();

	String createUsageAgreement();

	/*
	 * Factories
	 */
	void draftUsageAgreementsTextsFactory();

	void currentUsageAgreementsTextsFactory();

	/*
	 * Lifecycle
	 */
	void destroyCallback();

}
