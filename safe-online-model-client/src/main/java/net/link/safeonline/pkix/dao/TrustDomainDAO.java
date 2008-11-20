/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface TrustDomainDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "TrustDomainDAOBean/local";


    List<TrustDomainEntity> listTrustDomains();

    TrustDomainEntity addTrustDomain(String name, boolean performOcspCheck);

    TrustDomainEntity addTrustDomain(String name, boolean performOcspCheck, long ocspCachetimeOutMillis);

    TrustDomainEntity findTrustDomain(String name);

    TrustDomainEntity getTrustDomain(String name)
            throws TrustDomainNotFoundException;

    void removeTrustDomain(TrustDomainEntity trustDomain);
}
