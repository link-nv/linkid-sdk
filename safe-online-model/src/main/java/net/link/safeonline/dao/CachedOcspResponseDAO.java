/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.CachedOcspResponseEntity;
import net.link.safeonline.entity.TrustDomainEntity;

@Local
public interface CachedOcspResponseDAO {

	CachedOcspResponseEntity findCachedOcspResponse(String key);

	CachedOcspResponseEntity addCachedOcspResponse(String key, boolean result,
			TrustDomainEntity trustDomain);

	void removeCachedOcspResponse(CachedOcspResponseEntity cachedOcspResponse);

	void clearOcspCache();

	void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain);

}
