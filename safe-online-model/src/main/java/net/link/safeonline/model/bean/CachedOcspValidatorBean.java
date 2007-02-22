/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.dao.CachedOcspResponseDAO;
import net.link.safeonline.entity.CachedOcspResponseEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.CachedOcspValidator;
import net.link.safeonline.model.OcspValidator;
import net.link.safeonline.model.OcspValidator.OcspResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class CachedOcspValidatorBean implements CachedOcspValidator {

	private static final Log LOG = LogFactory
			.getLog(CachedOcspValidatorBean.class);

	@EJB
	OcspValidator ocspValidator;

	@EJB
	CachedOcspResponseDAO cachedOcspResponseDAO;

	public boolean performCachedOcspCheck(TrustDomainEntity trustDomain,
			X509Certificate certificate, X509Certificate issuerCertificate) {

		LOG.debug("performing cached OCSP lookup");

		OcspResult ocspResult;

		URI ocspURI = ocspValidator.getOcspUri(certificate);
		if (null == ocspURI) {
			return true;
		}

		String key = generateKey(certificate);

		if (key == null) {
			LOG.debug("Unable to generate cache key, skipping cache");
			ocspResult = ocspValidator.verifyOcspStatus(ocspURI, certificate,
					issuerCertificate);
			return convertOcspResult(ocspResult);
		}

		// Cache lookup
		CachedOcspResponseEntity cachedOcspResponse = cachedOcspResponseDAO
				.findCachedOcspResponse(key);

		// The response is not cached
		if (cachedOcspResponse == null) {
			LOG.debug("No cache entry found for key: " + key);
			ocspResult = ocspValidator.verifyOcspStatus(ocspURI, certificate,
					issuerCertificate);
			boolean result = convertOcspResult(ocspResult);
			if (cacheResult(ocspResult)) {
				cachedOcspResponseDAO.addCachedOcspResponse(key, result,
						trustDomain);
				LOG.debug("OCSP response cached for key: " + key);
			}
			return result;
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
		LOG.debug("trust domain timeout: " + trustDomain.getOcspCacheTimeOutMillis());
		if (timediff > trustDomain.getOcspCacheTimeOutMillis()) {
			LOG.debug("Cache entry expired for key: " + key);
			ocspResult = ocspValidator.verifyOcspStatus(ocspURI, certificate,
					issuerCertificate);
			boolean result = convertOcspResult(ocspResult);
			if (cacheResult(ocspResult)) {
				cachedOcspResponse.setEntryDate(new Date(System
						.currentTimeMillis()));
				cachedOcspResponse.setResult(result);
				LOG.debug("OCSP response updated for key: " + key);
			} else {
				cachedOcspResponseDAO
						.removeCachedOcspResponse(cachedOcspResponse);
				LOG.debug("Cache entry removed");
			}
			return result;
		}
		
		// .. or still valid
		LOG.debug("Cache hit for key: " + key);
		return cachedOcspResponse.getResult();

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

	private boolean convertOcspResult(OcspResult ocspResult) {
		if (ocspResult == OcspResult.GOOD) {
			return true;
		} else {
			return false;
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
