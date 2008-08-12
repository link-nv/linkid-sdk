/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.CachedOcspResultType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;

@Local
public interface CachedOcspResponseDAO {

    CachedOcspResponseEntity findCachedOcspResponse(String key);

    CachedOcspResponseEntity addCachedOcspResponse(String key,
            CachedOcspResultType result, TrustDomainEntity trustDomain);

    void removeCachedOcspResponse(CachedOcspResponseEntity cachedOcspResponse);

    void clearOcspCache();

    void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain);

    void clearOcspCacheExpiredForTrustDomain(TrustDomainEntity trustDomain);

}
