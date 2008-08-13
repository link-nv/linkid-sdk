/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.dao.TrustPointDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of application authentication service.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class ApplicationAuthenticationServiceBean implements ApplicationAuthenticationService {

    private static final Log LOG = LogFactory.getLog(ApplicationAuthenticationServiceBean.class);

    @EJB
    private ApplicationDAO   applicationDAO;

    @EJB
    private TrustPointDAO    trustPointDAO;


    public String authenticate(X509Certificate certificate) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(certificate);
        String applicationName = application.getName();
        LOG.debug("authenticated application: " + applicationName);
        return applicationName;
    }

    public List<X509Certificate> getCertificates(String applicationId) throws ApplicationNotFoundException {

        LOG.debug("get certificates for application Id: " + applicationId);
        ApplicationEntity application = this.applicationDAO.getApplication(applicationId);
        List<TrustPointEntity> trustPoints = this.trustPointDAO.listTrustPoints(application.getCertificateSubject());
        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (TrustPointEntity trustPoint : trustPoints) {
            certificates.add(trustPoint.getCertificate());

        }
        return certificates;
    }

    public boolean skipMessageIntegrityCheck(String applicationId) throws ApplicationNotFoundException {

        ApplicationEntity application = this.applicationDAO.getApplication(applicationId);
        boolean skipMessageIntegrityCheck = application.isSkipMessageIntegrityCheck();
        return skipMessageIntegrityCheck;
    }
}
