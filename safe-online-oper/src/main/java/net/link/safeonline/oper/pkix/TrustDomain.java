/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.exception.ExistingTrustDomainException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface TrustDomain {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "TrustDomainBean/local";


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

    String add()
            throws ExistingTrustDomainException;

    String removeTrustDomain()
            throws TrustDomainNotFoundException;

    String clearOcspCache();

    String clearOcspCachePerTrustDomain();

}
