/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.oper.app;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.UsageAgreementEntity;


@Local
public interface UsageAgreement {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "UsageAgreementBean/local";


    /*
     * Factories
     */
    void usageAgreementTextListFactory();

    void draftUsageAgreementsTextsFactory()
            throws ApplicationNotFoundException, PermissionDeniedException;

    void currentUsageAgreementsTextsFactory()
            throws PermissionDeniedException, ApplicationNotFoundException;

    /*
     * Actions
     */
    String viewText();

    String viewCurrentText();

    String viewDraftText();

    String saveText()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String createUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String editDraftText();

    String removeDraftText()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String editCurrentText()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String addText()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String releaseDraft()
            throws ApplicationNotFoundException, PermissionDeniedException;

    String removeDraft()
            throws ApplicationNotFoundException, PermissionDeniedException;

    /*
     * Accessors
     */
    UsageAgreementEntity getDraftUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException;

    UsageAgreementEntity getCurrentUsageAgreement()
            throws PermissionDeniedException, ApplicationNotFoundException;

    void setLanguage(String language);

    String getLanguage();

    List<String> autocompleteLanguage(Object event);

    /*
     * Lifecycle
     */
    void destroyCallback();

}
