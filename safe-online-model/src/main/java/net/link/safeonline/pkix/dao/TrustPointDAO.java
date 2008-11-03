/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.pkix.TrustPointPK;
import net.link.safeonline.pkix.exception.TrustPointNotFoundException;


@Local
public interface TrustPointDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/TrustPointDAOBean/local";

    void addTrustPoint(TrustDomainEntity trustDomain, X509Certificate certificate);

    List<TrustPointEntity> listTrustPoints(TrustDomainEntity trustDomain);

    TrustPointEntity getTrustPoint(TrustPointPK pk) throws TrustPointNotFoundException;

    TrustPointEntity findTrustPoint(TrustDomainEntity trustDomain, X509Certificate certificate);

    void removeTrustPoint(TrustPointEntity trustPoint);

    List<TrustPointEntity> listTrustPoints(String certificateSubject);
}
