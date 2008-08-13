/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementTextNotFoundException;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.entity.UsageAgreementTextPK;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class UsageAgreementDAOBean implements UsageAgreementDAO {

    private static final Log                          LOG = LogFactory.getLog(UsageAgreementDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                             entityManager;

    private UsageAgreementEntity.QueryInterface       queryObject;

    private UsageAgreementTextEntity.QueryInterface   textQueryObject;

    private GlobalUsageAgreementEntity.QueryInterface globalQueryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                UsageAgreementEntity.QueryInterface.class);

        this.textQueryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                UsageAgreementTextEntity.QueryInterface.class);
        this.globalQueryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                GlobalUsageAgreementEntity.QueryInterface.class);
    }

    public UsageAgreementEntity addUsageAgreement(ApplicationEntity application, Long usageAgreementVersion) {

        LOG.debug("add application usage agreement: " + application.getName() + " version=" + usageAgreementVersion);
        UsageAgreementEntity usageAgreement = new UsageAgreementEntity(application, usageAgreementVersion);
        this.entityManager.persist(usageAgreement);
        return usageAgreement;
    }

    public UsageAgreementEntity getUsageAgreement(ApplicationEntity application, Long usageAgreementVersion) {

        UsageAgreementPK usageAgreementPK = new UsageAgreementPK(application.getName(), usageAgreementVersion);
        UsageAgreementEntity usageAgreement = this.entityManager.find(UsageAgreementEntity.class, usageAgreementPK);
        if (null == usageAgreement) {
            LOG.debug("usage agreement version: " + usageAgreementVersion + " not found for application: "
                    + application.getName());
            return null;
        }
        return usageAgreement;
    }

    public UsageAgreementEntity findUsageAgreement(ApplicationEntity application, Long usageAgreementVersion)
            throws UsageAgreementNotFoundException {

        UsageAgreementEntity usageAgreement = getUsageAgreement(application, usageAgreementVersion);
        if (null == usageAgreement)
            throw new UsageAgreementNotFoundException(usageAgreementVersion);
        return usageAgreement;
    }

    public List<UsageAgreementEntity> listUsageAgreements(ApplicationEntity application) {

        LOG.debug("list usage agreements for application: " + application.getName());
        return this.queryObject.listUsageAgreements(application);
    }

    public void removeUsageAgreements(ApplicationEntity application) {

        LOG.debug("remove usage agreements for application: " + application.getName());
        List<UsageAgreementEntity> usageAgreements = listUsageAgreements(application);
        for (UsageAgreementEntity usageAgreement : usageAgreements) {
            this.entityManager.remove(usageAgreement);
        }

    }

    public void removeUsageAgreement(ApplicationEntity application, Long usageAgreementVersion) {

        LOG.debug("remove usage agreement for application: " + application.getName() + " version="
                + usageAgreementVersion);
        UsageAgreementEntity usageAgreement = this.getUsageAgreement(application, usageAgreementVersion);
        if (null == usageAgreement)
            return;
        this.entityManager.remove(usageAgreement);
        this.entityManager.flush();
        this.textQueryObject.removeUsageAgreementTexts(application.getName(), usageAgreementVersion);
    }

    public List<UsageAgreementTextEntity> listUsageAgreementTexts(ApplicationEntity application,
            Long usageAgreementVersion) {

        LOG.debug("list usage agreements texts for application: " + application.getName() + " and version: "
                + usageAgreementVersion);
        return this.textQueryObject.listUsageAgreementTexts(application.getName(), usageAgreementVersion);
    }

    public UsageAgreementTextEntity addUsageAgreementText(UsageAgreementEntity usageAgreement, String text,
            String language) {

        LOG.debug("add usage agreement text: language=" + language + " version="
                + usageAgreement.getUsageAgreementVersion());
        UsageAgreementTextEntity usageAgreementText = new UsageAgreementTextEntity(usageAgreement, text, language);
        this.entityManager.persist(usageAgreementText);
        usageAgreement.getUsageAgreementTexts().add(usageAgreementText);
        return usageAgreementText;
    }

    public void removeUsageAgreementText(UsageAgreementTextEntity usageAgreementText) {

        UsageAgreementPK usageAgreementPK = new UsageAgreementPK(usageAgreementText.getOwner(), usageAgreementText
                .getUsageAgreementVersion());
        UsageAgreementEntity usageAgreement = this.entityManager.find(UsageAgreementEntity.class, usageAgreementPK);
        usageAgreement.getUsageAgreementTexts().remove(usageAgreementText);
        this.entityManager.remove(usageAgreementText);
    }

    public UsageAgreementTextEntity findUsageAgreementText(UsageAgreementEntity usageAgreement, String language)
            throws UsageAgreementTextNotFoundException {

        UsageAgreementTextEntity usageAgreementText = getUsageAgreementText(usageAgreement, language);
        if (null == usageAgreementText)
            throw new UsageAgreementTextNotFoundException(usageAgreement.getUsageAgreementVersion());
        return usageAgreementText;
    }

    public UsageAgreementTextEntity getUsageAgreementText(UsageAgreementEntity usageAgreement, String language) {

        LOG.debug("get usage agreement text: language=" + language + " version="
                + usageAgreement.getUsageAgreementVersion());
        UsageAgreementTextPK usageAgreementTextPK = new UsageAgreementTextPK(usageAgreement.getApplication().getName(),
                usageAgreement.getUsageAgreementVersion(), language);
        UsageAgreementTextEntity usageAgreementText = this.entityManager.find(UsageAgreementTextEntity.class,
                usageAgreementTextPK);
        if (null == usageAgreementText) {
            LOG.debug("usage agreement text version; " + usageAgreement.getUsageAgreementVersion()
                    + " not found for application: " + usageAgreement.getApplication().getName());
            return null;
        }
        return usageAgreementText;
    }

    public GlobalUsageAgreementEntity addGlobalUsageAgreement(Long usageAgreementVersion) {

        LOG.debug("add global usage agreement: version=" + usageAgreementVersion);
        GlobalUsageAgreementEntity usageAgreement = new GlobalUsageAgreementEntity(usageAgreementVersion);
        this.entityManager.persist(usageAgreement);
        return usageAgreement;

    }

    public UsageAgreementTextEntity addGlobalUsageAgreementText(GlobalUsageAgreementEntity usageAgreement, String text,
            String language) {

        LOG.debug("add global usage agreement text: language=" + language + " version="
                + usageAgreement.getUsageAgreementVersion());
        UsageAgreementTextEntity usageAgreementText = new UsageAgreementTextEntity(usageAgreement, text, language);
        this.entityManager.persist(usageAgreementText);
        usageAgreement.getUsageAgreementTexts().add(usageAgreementText);
        return usageAgreementText;
    }

    public GlobalUsageAgreementEntity getGlobalUsageAgreement() {

        try {
            return this.globalQueryObject.getCurrentGlobalUsageAgreement();
        } catch (NoResultException e) {
            return null;
        }
    }

    public GlobalUsageAgreementEntity getGlobalUsageAgreement(Long usageAgreementVersion) {

        GlobalUsageAgreementEntity usageAgreement = this.entityManager.find(GlobalUsageAgreementEntity.class,
                usageAgreementVersion);
        if (null == usageAgreement) {
            LOG.debug("global usage agreement version: " + usageAgreementVersion + " not found");
            return null;
        }
        return usageAgreement;
    }

    public UsageAgreementTextEntity getGlobalUsageAgreementText(GlobalUsageAgreementEntity usageAgreement,
            String language) {

        LOG.debug("get usage agreement text: language=" + language + " version="
                + usageAgreement.getUsageAgreementVersion());
        UsageAgreementTextPK usageAgreementTextPK = new UsageAgreementTextPK(
                GlobalUsageAgreementEntity.GLOBAL_USAGE_AGREEMENT, usageAgreement.getUsageAgreementVersion(), language);
        UsageAgreementTextEntity usageAgreementText = this.entityManager.find(UsageAgreementTextEntity.class,
                usageAgreementTextPK);
        if (null == usageAgreementText) {
            LOG.debug("usage agreement text version; " + usageAgreement.getUsageAgreementVersion() + " not found for");
            return null;
        }
        return usageAgreementText;
    }

    public void removeGlobalUsageAgreement(Long usageAgreementVersion) {

        LOG.debug("remove global usage agreement: version=" + usageAgreementVersion);
        GlobalUsageAgreementEntity usageAgreement = this.getGlobalUsageAgreement(usageAgreementVersion);
        if (null == usageAgreement)
            return;
        this.entityManager.remove(usageAgreement);
        this.entityManager.flush();
        this.textQueryObject.removeUsageAgreementTexts(GlobalUsageAgreementEntity.GLOBAL_USAGE_AGREEMENT,
                usageAgreementVersion);
    }

    public void removeGlobalUsageAgreementText(UsageAgreementTextEntity usageAgreementText) {

        GlobalUsageAgreementEntity usageAgreement = this.getGlobalUsageAgreement(usageAgreementText
                .getUsageAgreementVersion());
        usageAgreement.getUsageAgreementTexts().remove(usageAgreementText);
        this.entityManager.remove(usageAgreementText);
    }

}
