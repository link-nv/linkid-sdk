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

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.WSSecurityUtil;
import net.link.safeonline.util.ee.IdentityServiceClient;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = WSSecurityConfiguration.JNDI_BINDING)
public class WSSecurityConfigurationBean implements WSSecurityConfiguration {

    @EJB(mappedName = ConfigurationManager.JNDI_BINDING)
    private ConfigurationManager             configurationManager;

    @EJB(mappedName = PkiValidator.JNDI_BINDING)
    private PkiValidator                     pkiValidator;

    @EJB(mappedName = ApplicationAuthenticationService.JNDI_BINDING)
    private ApplicationAuthenticationService applicationAuthenticationService;


    public long getMaximumWsSecurityTimestampOffset() {

        return this.configurationManager.getMaximumWsSecurityTimestampOffset();
    }

    public boolean skipMessageIntegrityCheck(X509Certificate certificate) {

        PkiResult result;
        try {
            result = this.pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate);
            if (PkiResult.VALID == result) {
                String applicationId = this.applicationAuthenticationService.authenticate(certificate);
                return this.applicationAuthenticationService.skipMessageIntegrityCheck(applicationId);
            }
        } catch (TrustDomainNotFoundException e) {
            throw WSSecurityUtil.createSOAPFaultException("application trust domain not found", "FailedAuthentication");
        } catch (ApplicationNotFoundException e) {
            throw WSSecurityUtil.createSOAPFaultException("unknown application", "FailedAuthentication");
        }
        if (PkiResult.VALID != result) {
            try {
                result = this.pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN, certificate);
                if (PkiResult.VALID == result)
                    return false;
            } catch (TrustDomainNotFoundException e) {
                throw WSSecurityUtil.createSOAPFaultException("devices trust domain not found", "FailedAuthentication");
            }
        }
        if (PkiResult.VALID != result) {
            try {
                result = this.pkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN, certificate);
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

        return new IdentityServiceClient().getCertificate();
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKey getPrivateKey() {

        return new IdentityServiceClient().getPrivateKey();
    }

}
