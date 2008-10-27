/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UsageAgreementServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.model.UsageAgreementManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of usage agreement service interface.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class UsageAgreementServiceBean implements UsageAgreementService, UsageAgreementServiceRemote {

    private static final Log      LOG = LogFactory.getLog(UsageAgreementServiceBean.class);

    @EJB
    private UsageAgreementManager usageAgreementManager;

    @EJB
    private UsageAgreementDAO     usageAgreementDAO;

    @EJB
    private SubjectManager        subjectManager;

    @EJB
    private ApplicationDAO        applicationDAO;

    @EJB
    private SubscriptionDAO       subscriptionDAO;

    @Resource
    private SessionContext        sessionContext;


    private void checkReadPermission(ApplicationEntity application) throws PermissionDeniedException {

        if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity expectedSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
        if (false == expectedSubject.equals(actualSubject))
            throw new PermissionDeniedException("application owner admin mismatch");
    }

    /**
     * Check write permission on the given application. Only the subject corresponding with the application owner of the
     * application is allowed to write to the application entity.
     * 
     * @param application
     * @throws PermissionDeniedException
     */
    private void checkWritePermission(ApplicationEntity application) throws PermissionDeniedException {

        if (this.sessionContext.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE))
            return;
        ApplicationOwnerEntity applicationOwner = application.getApplicationOwner();
        SubjectEntity requiredSubject = applicationOwner.getAdmin();
        SubjectEntity actualSubject = this.subjectManager.getCallerSubject();
        if (false == requiredSubject.equals(actualSubject))
            throw new PermissionDeniedException("application owner admin mismatch");
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public UsageAgreementEntity createDraftUsageAgreement(String applicationName, Long usageAgreementVersion)
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("create draft usage agreement for application: " + applicationName + " from version: "
                + usageAgreementVersion);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        UsageAgreementEntity draftUsageAgreement = this.usageAgreementDAO.addUsageAgreement(application,
                UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);

        UsageAgreementEntity usageAgreement = this.usageAgreementDAO.getUsageAgreement(application,
                usageAgreementVersion);
        if (null != usageAgreement) {
            for (UsageAgreementTextEntity usageAgreementText : usageAgreement.getUsageAgreementTexts()) {
                this.usageAgreementDAO.addUsageAgreementText(draftUsageAgreement, usageAgreementText.getText(),
                        usageAgreementText.getLanguage());
            }
        }
        return draftUsageAgreement;
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public UsageAgreementTextEntity createDraftUsageAgreementText(String applicationName, String language, String text)
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("create draft usage agreement text for application: " + applicationName + " language=" + language);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        UsageAgreementEntity usageAgreement = getDraftUsageAgreement(applicationName);
        UsageAgreementTextEntity usageAgreementText = usageAgreement.getUsageAgreementText(language);
        if (null == usageAgreementText) {
            usageAgreementText = this.usageAgreementDAO.addUsageAgreementText(usageAgreement, text, language);
        }
        return usageAgreementText;
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void updateUsageAgreement(String applicationName) throws ApplicationNotFoundException,
            PermissionDeniedException {

        LOG.debug("update application usage agreement: " + applicationName);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        this.usageAgreementManager.updateUsageAgreement(application);
    }

    @RolesAllowed( { SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public UsageAgreementEntity getCurrentUsageAgreement(String applicationName) throws PermissionDeniedException,
            ApplicationNotFoundException {

        LOG.debug("get current usage agreement for application: " + applicationName);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkReadPermission(application);

        return this.usageAgreementDAO.getUsageAgreement(application, application.getCurrentApplicationUsageAgreement());
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public List<UsageAgreementEntity> getUsageAgreements(String applicationName) throws ApplicationNotFoundException,
            PermissionDeniedException {

        LOG.debug("get usage agreements for application: " + applicationName);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkReadPermission(application);

        return this.usageAgreementDAO.listUsageAgreements(application);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public UsageAgreementEntity getDraftUsageAgreement(String applicationName) throws ApplicationNotFoundException,
            PermissionDeniedException {

        LOG.debug("get draft usage agreement for application: " + applicationName);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkReadPermission(application);

        return this.usageAgreementDAO.getUsageAgreement(application, UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void setDraftUsageAgreementText(String applicationName, String language, String text)
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("set draft usage agreement text for application: " + applicationName + " language=" + language);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        UsageAgreementEntity usageAgreement = this.usageAgreementDAO.getUsageAgreement(application,
                UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);

        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getUsageAgreementText(usageAgreement,
                language);
        usageAgreementText.setText(text);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void removeDraftUsageAgreementText(String applicationName, String language)
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("remove draft usage agreement text for application: " + applicationName + " language=" + language);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        UsageAgreementEntity usageAgreement = this.usageAgreementDAO.getUsageAgreement(application,
                UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getUsageAgreementText(usageAgreement,
                language);

        this.usageAgreementDAO.removeUsageAgreementText(usageAgreementText);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public void removeDraftUsageAgreement(String applicationName) throws ApplicationNotFoundException,
            PermissionDeniedException {

        LOG.debug("remove draft usage agreement for application: " + applicationName);

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        checkWritePermission(application);

        this.usageAgreementDAO.removeUsageAgreement(application, UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public boolean requiresUsageAgreementAcceptation(String applicationName, String language)
            throws ApplicationNotFoundException, SubscriptionNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("is confirmation required for application " + applicationName + " by subject " + subject.getUserId());

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        long currentUsageAgreementVersion = application.getCurrentApplicationUsageAgreement();
        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);

        long confirmedUsageAgreementVersion = subscription.getConfirmedUsageAgreementVersion();
        if (confirmedUsageAgreementVersion != currentUsageAgreementVersion
                && currentUsageAgreementVersion != GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION) {
            String text = getUsageAgreementText(applicationName, language);
            if (text.equals(""))
                return false;
            return true;
        }
        return false;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void confirmUsageAgreementVersion(String applicationName) throws ApplicationNotFoundException,
            SubscriptionNotFoundException {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("confirm usage agreement for application " + applicationName + " by subject " + subject.getUserId());

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        Long currentUsageAgreementVersion = application.getCurrentApplicationUsageAgreement();
        SubscriptionEntity subscription = this.subscriptionDAO.getSubscription(subject, application);
        subscription.setConfirmedUsageAgreementVersion(currentUsageAgreementVersion);

    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public String getUsageAgreementText(String applicationName, String language) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        Long usageAgreementVersion = application.getCurrentApplicationUsageAgreement();
        UsageAgreementEntity usageAgreement = this.usageAgreementDAO.getUsageAgreement(application,
                usageAgreementVersion);
        if (null == usageAgreement)
            return null;
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getUsageAgreementText(usageAgreement,
                language);
        if (null == usageAgreementText)
            return null;
        return usageAgreementText.getText();
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public String getUsageAgreementText(String applicationName, String language, Long usageAgreementVersion)
            throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationName);
        UsageAgreementEntity usageAgreement = this.usageAgreementDAO.getUsageAgreement(application,
                usageAgreementVersion);
        if (null == usageAgreement)
            return null;
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getUsageAgreementText(usageAgreement,
                language);
        if (null == usageAgreementText)
            return null;
        return usageAgreementText.getText();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity createDraftGlobalUsageAgreement() {

        LOG.debug("create draft global usage agreement");
        GlobalUsageAgreementEntity draftUsageAgreement = this.usageAgreementDAO
                .addGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO.getGlobalUsageAgreement();
        if (null != usageAgreement) {
            for (UsageAgreementTextEntity usageAgreementText : usageAgreement.getUsageAgreementTexts()) {
                this.usageAgreementDAO.addGlobalUsageAgreementText(draftUsageAgreement, usageAgreementText.getText(),
                        usageAgreementText.getLanguage());
            }
        }
        return draftUsageAgreement;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public UsageAgreementTextEntity createDraftGlobalUsageAgreementText(String language, String text) {

        LOG.debug("create draft usage agreement text: language=" + language);
        GlobalUsageAgreementEntity usageAgreement = getDraftGlobalUsageAgreement();
        UsageAgreementTextEntity usageAgreementText = usageAgreement.getUsageAgreementText(language);
        if (null == usageAgreementText) {
            usageAgreementText = this.usageAgreementDAO.addGlobalUsageAgreementText(usageAgreement, text, language);
        }
        return usageAgreementText;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void setDraftGlobalUsageAgreementText(String language, String text) {

        LOG.debug("set draft usage agreement text: language=" + language);

        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO
                .getGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);

        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getGlobalUsageAgreementText(
                usageAgreement, language);
        usageAgreementText.setText(text);
    }

    @RolesAllowed( { SafeOnlineRoles.OPERATOR_ROLE, SafeOnlineRoles.USER_ROLE })
    public GlobalUsageAgreementEntity getCurrentGlobalUsageAgreement() {

        LOG.debug("get current usage agreement");
        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO.getGlobalUsageAgreement();
        if (null == usageAgreement)
            return null;
        if (usageAgreement.getUsageAgreementVersion().longValue() == GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION
                .longValue())
            return null;
        return usageAgreement;

    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public GlobalUsageAgreementEntity getDraftGlobalUsageAgreement() {

        LOG.debug("get draft usage agreement");
        return this.usageAgreementDAO
                .getGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDraftGlobalUsageAgreement() {

        LOG.debug("remove draft usage agreement");

        this.usageAgreementDAO
                .removeGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDraftGlobalUsageAgreementText(String language) {

        LOG.debug("remove draft usage agreement text language=" + language);

        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO
                .getGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getGlobalUsageAgreementText(
                usageAgreement, language);

        this.usageAgreementDAO.removeGlobalUsageAgreementText(usageAgreementText);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateGlobalUsageAgreement() {

        LOG.debug("update global usage agreement");
        this.usageAgreementManager.updateGlobalUsageAgreement();
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public boolean requiresGlobalUsageAgreementAcceptation(String language) {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("is confirmation required by subject " + subject.getUserId());

        GlobalUsageAgreementEntity globalUsageAgreement = this.usageAgreementDAO.getGlobalUsageAgreement();
        if (null == globalUsageAgreement)
            return false;
        long currentUsageAgreementVersion = globalUsageAgreement.getUsageAgreementVersion();

        long confirmedUsageAgreementVersion = subject.getConfirmedUsageAgreementVersion();
        if (confirmedUsageAgreementVersion != currentUsageAgreementVersion
                && currentUsageAgreementVersion != GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION
                        .longValue()) {
            String text = getGlobalUsageAgreementText(language);
            if (text.equals(""))
                return false;
            return true;
        }
        return false;
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public void confirmGlobalUsageAgreementVersion() {

        SubjectEntity subject = this.subjectManager.getCallerSubject();
        LOG.debug("confirm global usage agreement for subject " + subject.getUserId());

        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO.getGlobalUsageAgreement();
        if (null == usageAgreement) {
            subject.setConfirmedUsageAgreementVersion(GlobalUsageAgreementEntity.EMPTY_GLOBAL_USAGE_AGREEMENT_VERSION);
        } else {
            subject.setConfirmedUsageAgreementVersion(usageAgreement.getUsageAgreementVersion());
        }
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public String getGlobalUsageAgreementText(String language) {

        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO.getGlobalUsageAgreement();
        if (null == usageAgreement)
            return null;
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getGlobalUsageAgreementText(
                usageAgreement, language);
        if (null == usageAgreementText)
            return null;
        return usageAgreementText.getText();
    }

    @RolesAllowed(SafeOnlineRoles.USER_ROLE)
    public String getGlobalUsageAgreementText(String language, Long usageAgreementVersion) {

        GlobalUsageAgreementEntity usageAgreement = this.usageAgreementDAO
                .getGlobalUsageAgreement(usageAgreementVersion);
        if (null == usageAgreement)
            return null;
        UsageAgreementTextEntity usageAgreementText = this.usageAgreementDAO.getGlobalUsageAgreementText(
                usageAgreement, language);
        if (null == usageAgreementText)
            return null;
        return usageAgreementText.getText();
    }

}
