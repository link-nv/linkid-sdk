/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.TrustPointNotFoundException;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;

@Local
public interface TrustPointDAO {

	void addTrustPoint(TrustDomainEntity trustDomain,
			X509Certificate certificate);

	List<TrustPointEntity> getTrustPoints(TrustDomainEntity trustDomain);

	TrustPointEntity getTrustPoint(TrustDomainEntity trustDomain,
			String subjectName) throws TrustPointNotFoundException;

	TrustPointEntity findTrustPoint(TrustDomainEntity trustDomain,
			String subjectName);

	void removeTrustPoint(TrustPointEntity trustPoint);
}
