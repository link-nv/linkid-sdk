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
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.model.UsageAgreementManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = UsageAgreementManager.JNDI_BINDING)
public class UsageAgreementManagerBean implements UsageAgreementManager {

    private final static Log  LOG = LogFactory.getLog(UsageAgreementManagerBean.class);

    @EJB(mappedName = UsageAgreementDAO.JNDI_BINDING)
    private UsageAgreementDAO usageAgreementDAO;


    public void setUsageAgreement(ApplicationEntity application, Long usageAgreementVersion)
            throws UsageAgreementNotFoundException {

        LOG.debug("set usage agreement for application: " + application.getName() + " to version: " + usageAgreementVersion);
        usageAgreementDAO.findUsageAgreement(application, usageAgreementVersion);
        application.setCurrentApplicationUsageAgreement(usageAgreementVersion);
    }

    public void updateUsageAgreement(ApplicationEntity application) {

        LOG.debug("update usage agreement for application: " + application.getName());
        UsageAgreementEntity draftUsageAgreement = usageAgreementDAO.getUsageAgreement(application,
                UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);
        long newUsageAgreementVersion;
        if (application.getCurrentApplicationUsageAgreement() == UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION) {
            newUsageAgreementVersion = UsageAgreementPK.INITIAL_USAGE_AGREEMENT_VERSION;
        } else {
            newUsageAgreementVersion = application.getCurrentApplicationUsageAgreement() + 1;
        }

        UsageAgreementEntity newUsageAgreement = usageAgreementDAO.addUsageAgreement(application, newUsageAgreementVersion);

        for (UsageAgreementTextEntity draftUsageAgreementText : draftUsageAgreement.getUsageAgreementTexts()) {
            usageAgreementDAO.addUsageAgreementText(newUsageAgreement, draftUsageAgreementText.getText(),
                    draftUsageAgreementText.getLanguage());
        }

        usageAgreementDAO.removeUsageAgreement(application, UsageAgreementPK.DRAFT_USAGE_AGREEMENT_VERSION);

        LOG.debug("update application to version: " + newUsageAgreementVersion);
        application.setCurrentApplicationUsageAgreement(newUsageAgreementVersion);
    }

    public void updateGlobalUsageAgreement() {

        LOG.debug("update global usage agreement");
        GlobalUsageAgreementEntity draftUsageAgreement = usageAgreementDAO
                                                                               .getGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
        GlobalUsageAgreementEntity currentUsageAgreement = usageAgreementDAO.getGlobalUsageAgreement();
        long newUsageAgreementVersion;
        long currentUsageAgreementVersion = currentUsageAgreement.getUsageAgreementVersion().longValue();
        if (currentUsageAgreementVersion == GlobalUsageAgreementEntity.EMPTY_GLOBAL_USAGE_AGREEMENT_VERSION.longValue()
                || currentUsageAgreementVersion == GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION.longValue()) {
            newUsageAgreementVersion = GlobalUsageAgreementEntity.INITIAL_GLOBAL_USAGE_AGREEMENT_VERSION;
        } else {
            newUsageAgreementVersion = currentUsageAgreement.getUsageAgreementVersion() + 1;
        }
        GlobalUsageAgreementEntity newUsageAgreement = usageAgreementDAO.addGlobalUsageAgreement(newUsageAgreementVersion);
        for (UsageAgreementTextEntity draftUsageAgreementText : draftUsageAgreement.getUsageAgreementTexts()) {
            usageAgreementDAO.addGlobalUsageAgreementText(newUsageAgreement, draftUsageAgreementText.getText(),
                    draftUsageAgreementText.getLanguage());
        }

        usageAgreementDAO.removeGlobalUsageAgreement(GlobalUsageAgreementEntity.DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION);
    }
}
