/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.ExistingTrustDomainException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface TrustDomain extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/TrustDomainBean/local";

    /*
     * Factories.
     */
    void trustDomainListFactory();

    TrustDomainEntity getNewTrustDomain();

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String view();

    String add() throws ExistingTrustDomainException;

    String removeTrustDomain() throws TrustDomainNotFoundException;

    String clearOcspCache();

    String clearOcspCachePerTrustDomain();

}
