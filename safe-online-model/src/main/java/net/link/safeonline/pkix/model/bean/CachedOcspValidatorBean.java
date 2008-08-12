/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model.bean;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.CachedOcspResultType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.CachedOcspResponseDAO;
import net.link.safeonline.pkix.model.CachedOcspValidator;
import net.link.safeonline.pkix.model.OcspValidator;
import net.link.safeonline.pkix.model.OcspValidator.OcspResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class CachedOcspValidatorBean implements CachedOcspValidator {

    private static final Log LOG = LogFactory
                                         .getLog(CachedOcspValidatorBean.class);

    @EJB
    OcspValidator            ocspValidator;

    @EJB
    CachedOcspResponseDAO    cachedOcspResponseDAO;


    public OcspResult performCachedOcspCheck(TrustDomainEntity trustDomain,
            X509Certificate certificate, X509Certificate issuerCertificate) {

        LOG.debug("performing cached OCSP lookup");

        OcspResult ocspResult;

        URI ocspURI = this.ocspValidator.getOcspUri(certificate);
        if (null == ocspURI) {
            return OcspResult.GOOD;
        }

        String key = generateKey(certificate);

        if (key == null) {
            LOG.debug("Unable to generate cache key, skipping cache");
            ocspResult = this.ocspValidator.verifyOcspStatus(ocspURI,
                    certificate, issuerCertificate);
            return ocspResult;
        }

        // Cache lookup
        CachedOcspResponseEntity cachedOcspResponse = this.cachedOcspResponseDAO
                .findCachedOcspResponse(key);

        // The response is not cached
        if (cachedOcspResponse == null) {
            LOG.debug("No cache entry found for key: " + key);
            ocspResult = this.ocspValidator.verifyOcspStatus(ocspURI,
                    certificate, issuerCertificate);
            CachedOcspResultType result = convertOcspResult(ocspResult);
            if (cacheResult(ocspResult)) {
                this.cachedOcspResponseDAO.addCachedOcspResponse(key, result,
                        trustDomain);
                LOG.debug("OCSP response cached for key: " + key);
            }
            return ocspResult;
        }

        // The response is cached
        LOG.debug("Cache entry found for key: " + key);
        long currentTime = System.currentTimeMillis();
        long cachedTime = cachedOcspResponse.getEntryDate().getTime();
        long timediff = currentTime - cachedTime;

        // ... either expired ...
        LOG.debug("cache entry time: " + cachedTime);
        LOG.debug("current time: " + currentTime);
        LOG.debug("timediff: " + timediff);
        LOG.debug("trust domain timeout: "
                + trustDomain.getOcspCacheTimeOutMillis());
        if (timediff > trustDomain.getOcspCacheTimeOutMillis()) {
            LOG.debug("Cache entry expired for key: " + key);
            ocspResult = this.ocspValidator.verifyOcspStatus(ocspURI,
                    certificate, issuerCertificate);
            CachedOcspResultType result = convertOcspResult(ocspResult);
            if (cacheResult(ocspResult)) {
                cachedOcspResponse.setEntryDate(new Date(System
                        .currentTimeMillis()));
                cachedOcspResponse.setResult(result);
                LOG.debug("OCSP response updated for key: " + key);
            } else {
                this.cachedOcspResponseDAO
                        .removeCachedOcspResponse(cachedOcspResponse);
                LOG.debug("Cache entry removed");
            }
            return ocspResult;
        }

        // .. or still valid
        LOG.debug("Cache hit for key: " + key);
        return convertCachedOcspResultType(cachedOcspResponse.getResult());
    }

    private boolean cacheResult(OcspResult ocspResult) {

        if (ocspResult == OcspResult.GOOD) {
            return true;
        }
        if (ocspResult == OcspResult.REVOKED) {
            return true;
        }
        return false;
    }

    private OcspResult convertCachedOcspResultType(
            CachedOcspResultType cachedOcspResult) {

        if (cachedOcspResult == CachedOcspResultType.GOOD) {
            return OcspResult.GOOD;
        } else if (cachedOcspResult == CachedOcspResultType.REVOKED) {
            return OcspResult.REVOKED;
        } else if (cachedOcspResult == CachedOcspResultType.SUSPENDED) {
            return OcspResult.SUSPENDED;
        } else {
            return OcspResult.UNKNOWN;
        }
    }

    private CachedOcspResultType convertOcspResult(OcspResult ocspResult) {

        if (ocspResult == OcspResult.GOOD) {
            return CachedOcspResultType.GOOD;
        } else if (ocspResult == OcspResult.REVOKED) {
            return CachedOcspResultType.REVOKED;
        } else if (ocspResult == OcspResult.SUSPENDED) {
            return CachedOcspResultType.SUSPENDED;
        } else {
            return CachedOcspResultType.UNKNOWN;
        }
    }

    private String generateKey(X509Certificate certificate) {

        String result = null;
        try {
            result = DigestUtils.shaHex(certificate.getEncoded());
        } catch (Exception e) {
            return null;
        }
        return result;
    }

}
