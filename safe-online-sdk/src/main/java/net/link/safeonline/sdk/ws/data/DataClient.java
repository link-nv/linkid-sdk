/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.net.ConnectException;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;

public interface DataClient {

	/**
	 * Sets the value of an attribute. Please notice that the attribute should
	 * already be defined via: {@link #createAttribute(String, String)}.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @param attributeValue
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 * @see #createAttribute(String, String)
	 */
	void setAttributeValue(String subjectLogin, String attributeName,
			String attributeValue) throws ConnectException;

	/**
	 * Gives back the attribute value of an attribute. We return a
	 * {@link DataValue} object to be able to make a distinction between a
	 * missing attribute and a <code>null</code> attribute value.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @return
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 * @throws RequestDeniedException
	 * @throws SubjectNotFoundException
	 */
	DataValue getAttributeValue(String subjectLogin, String attributeName)
			throws ConnectException, RequestDeniedException,
			SubjectNotFoundException;

	void createAttribute(String subjectLogin, String attributeName)
			throws ConnectException;
}
