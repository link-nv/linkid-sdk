/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.attrib;

import java.net.ConnectException;

/**
 * Interface for attribute client.
 * 
 * @author fcorneli
 * 
 */
public interface AttributeClient {

	/**
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws AttributeNotFoundException
	 * @throws RequestDeniedException
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 */
	String getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, RequestDeniedException,
			ConnectException;
}
