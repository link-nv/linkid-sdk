/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementTextEntity;


/**
 * Interface to service for retrieving information about applications' usage agreements.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface UsageAgreementService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "UsageAgreementServiceBean/local";


    /**
     * Create draft usage agreement from the specified version.
     * 
     * @param applicationId
     * @throws PermissionDeniedException
     * @throws ApplicationNotFoundException
     */
    UsageAgreementEntity createDraftUsageAgreement(long applicationId, Long usageAgreementVersion)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Commits the draft usage agreement to a new version.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void updateUsageAgreement(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Returns currently associated usage agreement with the specified application.
     * 
     * @param applicationId
     * @throws PermissionDeniedException
     * @throws ApplicationNotFoundException
     */
    UsageAgreementEntity getCurrentUsageAgreement(long applicationId)
            throws PermissionDeniedException, ApplicationNotFoundException;

    /**
     * Returns current draft usage agreement with the specified application. Returns null if no draft is present.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    UsageAgreementEntity getDraftUsageAgreement(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Get all usage agreements of the specified application.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    List<UsageAgreementEntity> getUsageAgreements(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Create ( if not already created ) new draft usage agreement text for the specified language.
     * 
     * @param applicationId
     * @param language
     * @param text
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    UsageAgreementTextEntity createDraftUsageAgreementText(long applicationId, String language, String text)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Set draft usage agreement text for the specified application and language.
     * 
     * @param applicationId
     * @param language
     * @param text
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void setDraftUsageAgreementText(long applicationId, String language, String text)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Remove draft usage agreement text for the specified application and language.
     * 
     * @param applicationId
     * @param language
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void removeDraftUsageAgreementText(long applicationId, String language)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Remove draft usage agreement for the specified application.
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     * @throws PermissionDeniedException
     */
    void removeDraftUsageAgreement(long applicationId)
            throws ApplicationNotFoundException, PermissionDeniedException;

    /**
     * Check whether the authenticating subject's subscription to the specified application conforms with the application's current usage
     * agreement version.
     * 
     * @param applicationId
     * @param language
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     */
    boolean requiresUsageAgreementAcceptation(long applicationId, String language)
            throws ApplicationNotFoundException, SubscriptionNotFoundException;

    /**
     * Confirm current usage agreement for specified application.
     * 
     * TODO: Version specified as application owner might be changing the current version while a user is confirming...
     * 
     * @param applicationId
     * @throws ApplicationNotFoundException
     * @throws SubscriptionNotFoundException
     */
    void confirmUsageAgreementVersion(long applicationId)
            throws ApplicationNotFoundException, SubscriptionNotFoundException;

    /**
     * Get application's usage agreement text for specified language
     * 
     * @throws ApplicationNotFoundException
     */
    String getUsageAgreementText(long applicationId, String language)
            throws ApplicationNotFoundException;

    /**
     * Get version of application's usage agreement texts for specified language.
     * 
     * @param applicationId
     * @param language
     * @param usageAgreementVersion
     * @throws ApplicationNotFoundException
     */
    String getUsageAgreementText(long applicationId, String language, Long usageAgreementVersion)
            throws ApplicationNotFoundException;

    /**
     * Commit the draft global usage agreement to a new version.
     */
    void updateGlobalUsageAgreement();

    /**
     * Remove current draft global usage agreement.
     */
    void removeDraftGlobalUsageAgreement();

    /**
     * Create ( if not already created ) new draft global usage agreement text for the specified language.
     * 
     * @param language
     * @param text
     */
    UsageAgreementTextEntity createDraftGlobalUsageAgreementText(String language, String text);

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
     */
    GlobalUsageAgreementEntity createDraftGlobalUsageAgreement();

    /**
     * Return draft global usage agreement.
     * 
     */
    GlobalUsageAgreementEntity getDraftGlobalUsageAgreement();

    /**
     * Return current global usage agreement.
     * 
     */
    GlobalUsageAgreementEntity getCurrentGlobalUsageAgreement();

    /**
     * Check whether authenticating subject has accepted the global usage agreement.
     * 
     * @param language
     * 
     */
    boolean requiresGlobalUsageAgreementAcceptation(String language);

    /**
     * Authenticating subject confirms to the global usage agreement
     */
    void confirmGlobalUsageAgreementVersion();

    /**
     * Get current global usage agreement text for specified language
     * 
     * @param language
     */
    String getGlobalUsageAgreementText(String language);

    /**
     * Get current global usage agreement text for specified language and version.
     * 
     * @param language
     */
    String getGlobalUsageAgreementText(String language, Long usageAgreementVersion);
}
