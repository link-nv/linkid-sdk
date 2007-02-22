/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface TrustDomain {

	void trustDomainListFactory();

	String view();

	void destroyCallback();

	String getName();

	void setName(String name);

	String add();

	String removeTrustDomain();

	boolean isPerformOcspCheck();

	void setPerformOcspCheck(boolean performOcspCheck);

	long getOcspCacheTimeOutMillis();

	void setOcspCacheTimeOutMillis(long ocspCachetimeOutMillis);
	
	String clearOcspCache();
	
	String clearOcspCachePerTrustDomain();

}
