/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.bean;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityUtil;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Configurable
@LocalBinding(jndiBinding = WSSecurityConfiguration.JNDI_BINDING)
@Interceptors( { ConfigurationInterceptor.class })
public class WSSecurityConfigurationBean implements WSSecurityConfiguration {

    public static final String               WS_SECURITY_MAX_TIMESTAMP_OFFSET = "Maximum WS-Security Timestamp Offset (ms)";

    /**
     * Maximum Offset between the WS-Security Created time and the time indicated by the local clock.
     */
    @Configurable(group = "Security", name = WS_SECURITY_MAX_TIMESTAMP_OFFSET)
    private Long                             maxWsSecurityTimestampOffset     = 1000 * 60 * 5L;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator                     pkiValidator;

    @EJB(mappedName = ApplicationAuthenticationService.JNDI_BINDING)
    private ApplicationAuthenticationService applicationAuthenticationService;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO                   applicationDAO;


    public long getMaximumWsSecurityTimestampOffset() {

        return maxWsSecurityTimestampOffset;
    }

    public boolean skipMessageIntegrityCheck(String applicationName)
            throws ApplicationNotFoundException {

        ApplicationEntity application = applicationDAO.getApplication(applicationName);
        boolean skipMessageIntegrityCheck = application.isSkipMessageIntegrityCheck();
        return skipMessageIntegrityCheck;
    }

    public boolean skipMessageIntegrityCheck(X509Certificate certificate) {

        PkiResult result;
        try {
            result = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate);
            if (PkiResult.VALID == result) {
                long applicationId = applicationAuthenticationService.authenticate(certificate);
                return applicationAuthenticationService.skipMessageIntegrityCheck(applicationId);
            }
        } catch (TrustDomainNotFoundException e) {
            throw WSSecurityUtil.createSOAPFaultException("application trust domain not found", "FailedAuthentication");
        } catch (ApplicationNotFoundException e) {
            throw WSSecurityUtil.createSOAPFaultException("unknown application", "FailedAuthentication");
        }
        if (PkiResult.VALID != result) {
            try {
                result = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN, certificate);
                if (PkiResult.VALID == result)
                    return false;
            } catch (TrustDomainNotFoundException e) {
                throw WSSecurityUtil.createSOAPFaultException("devices trust domain not found", "FailedAuthentication");
            }
        }
        if (PkiResult.VALID != result) {
            try {
                result = pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN, certificate);
                if (PkiResult.VALID == result)
                    return false;
            } catch (TrustDomainNotFoundException e) {
                throw WSSecurityUtil.createSOAPFaultException("olas trust domain not found", "FailedAuthentication");
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public X509Certificate getCertificate() {

        return new SafeOnlineNodeKeyStore().getCertificate();
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKey getPrivateKey() {

        return new SafeOnlineNodeKeyStore().getPrivateKey();
    }

}
