/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;

/**
 * Interface to service for retrieving information about applications' usage
 * agreements.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface UsageAgreementService {

	/**
	 * Create draft usage agreement from the specified version.
	 * 
	 * @param applicationName
	 * @throws PermissionDeniedException
	 * @throws ApplicationNotFoundException
	 */
	UsageAgreementEntity createDraftUsageAgreement(String applicationName,
			Long usageAgreementVersion) throws ApplicationNotFoundException,
			PermissionDeniedException;

	/**
	 * Commits the draft usage agreement to a new version.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void updateUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Returns currently associated usage agreement with the specified
	 * application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws PermissionDeniedException
	 * @throws ApplicationNotFoundException
	 */
	UsageAgreementEntity getCurrentUsageAgreement(String applicationName)
			throws PermissionDeniedException, ApplicationNotFoundException;

	/**
	 * Returns current draft usage agreement with the specified application.
	 * Returns null if no draft is present.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	UsageAgreementEntity getDraftUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Get all usage agreements of the specified application.
	 * 
	 * @param applicationName
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	List<UsageAgreementEntity> getUsageAgreements(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Create ( if not already created ) new draft usage agreement text for the
	 * specified language.
	 * 
	 * @param name
	 * @param language
	 * @param text
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	UsageAgreementTextEntity createDraftUsageAgreementText(
			String applicationName, String language, String text)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Set draft usage agreement text for the specified application and
	 * language.
	 * 
	 * @param name
	 * @param language
	 * @param text
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void setDraftUsageAgreementText(String applicatioName, String language,
			String text) throws ApplicationNotFoundException,
			PermissionDeniedException;

	/**
	 * Remove draft usage agreement text for the specified application and
	 * language.
	 * 
	 * @param applicationName
	 * @param language
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void removeDraftUsageAgreementText(String applicationName, String language)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Remove draft usage agreement for the specified application.
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws PermissionDeniedException
	 */
	void removeDraftUsageAgreement(String applicationName)
			throws ApplicationNotFoundException, PermissionDeniedException;

	/**
	 * Check whether the authenticating subject's subscription to the specified
	 * application conforms with the application's current usage agreement
	 * version.
	 * 
	 * @param applicationId
	 * @return
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 */
	boolean requiresUsageAgreementAcceptation(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException;

	/**
	 * Confirm current usage agreement for specified application.
	 * 
	 * TODO: Version specified as application owner might be changing the
	 * current version while a user is confirming...
	 * 
	 * @param applicationName
	 * @throws ApplicationNotFoundException
	 * @throws SubscriptionNotFoundException
	 */
	void confirmUsageAgreementVersion(String applicationName)
			throws ApplicationNotFoundException, SubscriptionNotFoundException;

	/**
	 * Get application's usage agreement text for specified language
	 * 
	 * @throws ApplicationNotFoundException
	 */
	String getUsageAgreementText(String applicationName, String language)
			throws ApplicationNotFoundException;

	/**
	 * Get version of application's usage agreement texts for specified
	 * language.
	 * 
	 * @param applicationName
	 * @param language
	 * @param usageAgreementVersion
	 * @return
	 * @throws ApplicationNotFoundException
	 */
	String getUsageAgreementText(String applicationName, String language,
			Long usageAgreementVersion) throws ApplicationNotFoundException;

	/**
	 * Commit the draft global usage agreement to a new version.
	 */
	void updateGlobalUsageAgreement();

	/**
	 * Remove current draft global usage agreement.
	 */
	void removeDraftGlobalUsageAgreement();

	/**
	 * Create ( if not already created ) new draft global usage agreement text
	 * for the specified language.
	 * 
	 * @param language
	 * @param text
	 * @return
	 */
	UsageAgreementTextEntity createDraftGlobalUsageAgreementText(
			String language, String text);

	/**
	 * Set draft global usage agreement text for the specified language.
	 * 
	 * @param language
	 * @param text
	 */
	void setDraftGlobalUsageAgreementText(String language, String text);

	/**
	 * Remove draft global usage agreement text for the specified language.
	 * 
	 * @param language
	 */
	void removeDraftGlobalUsageAgreementText(String language);

	/**
	 * Create draft usage agreement from the specified version.
	 * 
	 * @param currentUsageAgreement
	 * @return
	 */
	GlobalUsageAgreementEntity createDraftGlobalUsageAgreement();

	/**
	 * Return draft global usage agreement.
	 * 
	 * @return
	 */
	GlobalUsageAgreementEntity getDraftGlobalUsageAgreement();

	/**
	 * Return current global usage agreement.
	 * 
	 * @return
	 */
	GlobalUsageAgreementEntity getCurrentGlobalUsageAgreement();

	/**
	 * Check whether authenticating subject has accepted the global usage
	 * agreement.
	 * 
	 * @return
	 */
	boolean requiresGlobalUsageAgreementAcceptation();

	/**
	 * Authenticating subject confirms to the global usage agreement
	 */
	void confirmGlobalUsageAgreementVersion();

	/**
	 * Get current global usage agreement text for specified language
	 * 
	 * @param language
	 * @return
	 */
	String getGlobalUsageAgreementText(String language);
}
