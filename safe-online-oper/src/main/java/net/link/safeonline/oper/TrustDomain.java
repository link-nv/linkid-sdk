/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

import net.link.safeonline.entity.TrustDomainEntity;

@Local
public interface TrustDomain {

	void trustDomainListFactory();

	String view();

	void destroyCallback();

	String add();

	String removeTrustDomain();

	String clearOcspCache();

	String clearOcspCachePerTrustDomain();

	TrustDomainEntity getNewTrustDomain();
}
