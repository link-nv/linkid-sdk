/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.entity.TrustDomainEntity;

@Local
public interface TrustDomainDAO {

	List<TrustDomainEntity> getTrustDomains();

	void addTrustDomain(String name, boolean performOcspCheck);

	TrustDomainEntity findTrustDomain(String name);

	TrustDomainEntity getTrustDomain(String name)
			throws TrustDomainNotFoundException;

	void removeTrustDomain(TrustDomainEntity trustDomain);
}
