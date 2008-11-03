/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.pkix;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;


@Local
public interface EditTrustDomain extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/EditTrustDomainBean/local";

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String edit();

    String save() throws TrustDomainNotFoundException;

    String cancel();

    /*
     * Accessors.
     */
    boolean isPerformOcspCheck();

    void setPerformOcspCheck(boolean performOcspCheck);

    long getOcspCacheTimeOutMillis();

    void setOcspCacheTimeOutMillis(long ocspCacheTimeOutMillis);

}
