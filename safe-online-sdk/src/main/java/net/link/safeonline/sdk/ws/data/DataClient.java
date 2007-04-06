/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.net.ConnectException;

import net.link.safeonline.sdk.exception.RequestDeniedException;

public interface DataClient {

	/**
	 * @param subjectLogin
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 */
	String setAttributeValue(String subjectLogin, String attributeName,
			String attributeValue) throws ConnectException;

	/**
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 * @throws RequestDeniedException
	 */
	String getAttributeValue(String subjectLogin, String attributeName)
			throws ConnectException, RequestDeniedException;
}
