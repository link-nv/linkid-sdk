/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper;

import javax.ejb.Local;

@Local
public interface EditTrustDomain {

	String edit();

	void destroyCallback();

	String save();

	String cancel();

	boolean isPerformOcspCheck();

	void setPerformOcspCheck(boolean performOcspCheck);

	long getOcspCacheTimeOutMillis();

	void setOcspCacheTimeOutMillis(long ocspCacheTimeOutMillis);

}
