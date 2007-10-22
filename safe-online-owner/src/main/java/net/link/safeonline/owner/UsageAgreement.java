/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.owner;

import javax.ejb.Local;

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
	boolean getDraftUsageAgreementIsEmpty();

	boolean getCurrentUsageAgreementIsEmpty();

	void setLanguage(String language);

	String getLanguage();

	/*
	 * Lifecycle
	 */
	void destroyCallback();

}
