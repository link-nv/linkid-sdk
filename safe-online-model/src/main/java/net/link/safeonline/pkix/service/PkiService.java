/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.pkix.exception.ExistingTrustDomainException;
import net.link.safeonline.pkix.exception.ExistingTrustPointException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;


/**
 * Interface definition for PKI service component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface PkiService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "PkiServiceBean/local";


    /**
     * Gives back a list of all trust domains.
     * 
     */
    List<TrustDomainEntity> listTrustDomains();

    /**
     * Adds a trust domain with the given name.
     * 
     * @param name
     * @param performOcspCheck
     *            <code>true</code> is the certificate validator should perform an OCSP check when OCSP access location information is
     *            available within a certificate.
     * @throws ExistingTrustDomainException
     */
    void addTrustDomain(String name, boolean performOcspCheck)
            throws ExistingTrustDomainException;

    /**
     * Adds a trust domain with the given name.
     * 
     * @param name
     * @param performOcspCheck
     * @param ocspCacheTimeOutMillis
     * 
     *            <code>true</code> is the certificate validator should perform an OCSP check when OCSP access location information is
     *            available within a certificate.
     * @throws ExistingTrustDomainException
     */
    void addTrustDomain(String name, boolean performOcspCheck, long ocspCacheTimeOutMillis)
            throws ExistingTrustDomainException;

    /**
     * Removes a trust domain with the given name.
     * 
     * @param name
     * @throws TrustDomainNotFoundException
     */
    void removeTrustDomain(String name)
            throws TrustDomainNotFoundException;

    /**
     * Adds a trust point to a certain trust domain.
     * 
     * @param domainName
     * @param encodedCertificate
     * @throws TrustDomainNotFoundException
     * @throws CertificateEncodingException
     * @throws ExistingTrustPointException
     */
    void addTrustPoint(String domainName, byte[] encodedCertificate)
            throws TrustDomainNotFoundException, CertificateEncodingException, ExistingTrustPointException;

    /**
     * Gives back all trust points within a given domain.
     * 
     * @param domainName
     * @throws TrustDomainNotFoundException
     */
    List<TrustPointEntity> listTrustPoints(String domainName)
            throws TrustDomainNotFoundException;

    /**
     * Removes a trust point from a trust domain.
     * 
     * @param trustPoint
     * @throws TrustPointNotFoundException
     */
    void removeTrustPoint(TrustPointEntity trustPoint)
            throws TrustPointNotFoundException;

    /**
     * Gives back a trust domain for a given trust domain name.
     * 
     * @param trustDomainName
     * @throws TrustDomainNotFoundException
     */
    TrustDomainEntity getTrustDomain(String trustDomainName)
            throws TrustDomainNotFoundException;

    void saveTrustDomain(TrustDomainEntity trustDomain)
            throws TrustDomainNotFoundException;

    void clearOcspCache();

    void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain);
}
