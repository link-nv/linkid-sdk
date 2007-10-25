/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.owner;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.UsageAgreementEntity;

@Local
public interface UsageAgreement {

	/*
	 * Factories
	 */
	void usageAgreementTextListFactory();

	void draftUsageAgreementsTextsFactory();

	void currentUsageAgreementsTextsFactory();

	/*
	 * Actions
	 */
	String viewText();

	String viewCurrentText();

	String viewDraftText();

	String saveText();

	String createUsageAgreement();

	String editDraftText();

	String removeDraftText();

	String editCurrentText();

	String addText();

	String releaseDraft();

	String removeDraft();

	/*
	 * Accessors
	 */
	UsageAgreementEntity getDraftUsageAgreement();

	UsageAgreementEntity getCurrentUsageAgreement();

	void setLanguage(String language);

	String getLanguage();

	List<String> autocompleteLanguage(Object event);

	/*
	 * Lifecycle
	 */
	void destroyCallback();

}
