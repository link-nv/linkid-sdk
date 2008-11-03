/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model;

import java.security.cert.X509Certificate;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.model.OcspValidator.OcspResult;


@Local
public interface CachedOcspValidator extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/CachedOcspValidatorBean/local";

    /**
     * Given an X509 certificate and its issuerCertificate, validates the certificate using OCSP
     * 
     * @param certificate
     * @param issuerCertificate
     */
    OcspResult performCachedOcspCheck(TrustDomainEntity trustDomain, X509Certificate certificate, X509Certificate issuerCertificate);

}
