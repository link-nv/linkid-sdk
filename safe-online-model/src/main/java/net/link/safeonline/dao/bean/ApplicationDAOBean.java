/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.awt.Color;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class ApplicationDAOBean implements ApplicationDAO {

    private static final Log                 LOG = LogFactory.getLog(ApplicationDAOBean.class);

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager                    entityManager;

    private ApplicationEntity.QueryInterface queryObject;


    @PostConstruct
    public void postConstructCallback() {

        this.queryObject = QueryObjectFactory.createQueryObject(this.entityManager,
                ApplicationEntity.QueryInterface.class);
    }

    public ApplicationEntity findApplication(String applicationName) {

        LOG.debug("find application: " + applicationName);
        ApplicationEntity application = this.entityManager.find(ApplicationEntity.class, applicationName);
        return application;
    }

    public ApplicationEntity addApplication(String applicationName, String applicationFriendlyName,
            ApplicationOwnerEntity applicationOwner, String description, URL applicationUrl, byte[] applicationLogo,
            Color applicationColor, X509Certificate certificate) {

        LOG.debug("adding application: " + applicationName);
        ApplicationEntity application = new ApplicationEntity(applicationName, applicationFriendlyName,
                applicationOwner, description, applicationUrl, applicationLogo, applicationColor, certificate);
        this.entityManager.persist(application);
        return application;
    }

    public List<ApplicationEntity> listApplications() {

        List<ApplicationEntity> applications = this.queryObject.listApplications();
        return applications;
    }

    public List<ApplicationEntity> listUserApplications() {

        List<ApplicationEntity> applications = this.queryObject.listUserApplications();
        return applications;
    }

    public ApplicationEntity getApplication(String applicationName) throws ApplicationNotFoundException {

        ApplicationEntity application = findApplication(applicationName);
        if (null == application)
            throw new ApplicationNotFoundException();
        return application;
    }

    public ApplicationEntity addApplication(String applicationName, String applicationFriendlyName,
            ApplicationOwnerEntity applicationOwner, boolean allowUserSubscription, boolean removable,
            String description, URL applicationUrl, byte[] applicationLogo, Color applicationColor,
            X509Certificate certificate, long initialIdentityVersion, long usageAgreementVersion) {

        LOG.debug("adding application: " + applicationName);
        ApplicationEntity application = new ApplicationEntity(applicationName, applicationFriendlyName,
                applicationOwner, description, applicationUrl, applicationLogo, applicationColor,
                allowUserSubscription, removable, certificate, initialIdentityVersion, usageAgreementVersion);
        this.entityManager.persist(application);
        return application;
    }

    public void removeApplication(ApplicationEntity application) {

        LOG.debug("remove application(DAO): " + application.getName());
        this.entityManager.remove(application);
    }

    public List<ApplicationEntity> listApplications(ApplicationOwnerEntity applicationOwner) {

        LOG.debug("get application for application owner: " + applicationOwner.getName());
        List<ApplicationEntity> applications = this.queryObject.listApplicationsWhereApplicationOwner(applicationOwner);
        return applications;
    }

    public ApplicationEntity getApplication(X509Certificate certificate) throws ApplicationNotFoundException {

        List<ApplicationEntity> applications = this.queryObject.listApplicationsWhereCertificateSubject(certificate
                .getSubjectX500Principal().getName());
        if (applications.isEmpty()) {
            throw new ApplicationNotFoundException();
        }
        ApplicationEntity application = applications.get(0);
        return application;
    }

    public ApplicationEntity findApplication(X509Certificate certificate) {

        List<ApplicationEntity> applications = this.queryObject.listApplicationsWhereCertificateSubject(certificate
                .getSubjectX500Principal().getName());
        if (applications.isEmpty()) {
            return null;
        }
        ApplicationEntity application = applications.get(0);
        return application;

    }
}
