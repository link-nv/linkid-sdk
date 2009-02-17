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
import org.jboss.annotation.ejb.LocalBinding;


/**
 * Implementation of application authentication service.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@LocalBinding(jndiBinding = ApplicationAuthenticationService.JNDI_BINDING)
public class ApplicationAuthenticationServiceBean implements ApplicationAuthenticationService {

    private static final Log LOG = LogFactory.getLog(ApplicationAuthenticationServiceBean.class);

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO   applicationDAO;

    @EJB(mappedName = TrustPointDAO.JNDI_BINDING)
    private TrustPointDAO    trustPointDAO;


    public long authenticate(X509Certificate certificate)
            throws ApplicationNotFoundException {

        ApplicationEntity application = applicationDAO.getApplication(certificate);
        long applicationId = application.getId();
        LOG.debug("authenticated application: " + applicationId);
        return applicationId;
    }

    public List<X509Certificate> getCertificates(long applicationId)
            throws ApplicationNotFoundException {

        LOG.debug("get certificates for application Id: " + applicationId);
        ApplicationEntity application = applicationDAO.getApplication(applicationId);
        List<TrustPointEntity> trustPoints = trustPointDAO.listTrustPoints(application.getCertificateSubject());
        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (TrustPointEntity trustPoint : trustPoints) {
            certificates.add(trustPoint.getCertificate());

        }
        return certificates;
    }

    public List<X509Certificate> getCertificates(String applicationName)
            throws ApplicationNotFoundException {

        LOG.debug("get certificates for application: " + applicationName);
        ApplicationEntity application = applicationDAO.getApplication(applicationName);
        List<TrustPointEntity> trustPoints = trustPointDAO.listTrustPoints(application.getCertificateSubject());
        List<X509Certificate> certificates = new LinkedList<X509Certificate>();
        for (TrustPointEntity trustPoint : trustPoints) {
            certificates.add(trustPoint.getCertificate());

        }
        return certificates;
    }

    public boolean skipMessageIntegrityCheck(long applicationId)
            throws ApplicationNotFoundException {

        ApplicationEntity application = applicationDAO.getApplication(applicationId);
        boolean skipMessageIntegrityCheck = application.isSkipMessageIntegrityCheck();
        return skipMessageIntegrityCheck;
    }
}
