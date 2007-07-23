/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.net.ConnectException;

import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.MessageAccessor;

/**
 * Interface for data client component. Via this interface application can
 * perform CRUD operations on attributes of a subject. For this the application
 * must be an attribute provider. Only the operator can set the attribute
 * provider role for applications.
 * 
 * @author fcorneli
 * 
 */
public interface DataClient extends MessageAccessor {

	/**
	 * Sets the value of an attribute. Please notice that the attribute should
	 * already be defined via: {@link #createAttribute(String, String)}. The
	 * attribute value can be of type {@link String} or {@link Boolean}.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @param attributeValue
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 * @throws AttributeNotFoundException
	 *             in case the attribute entity did not exist.
	 * @see #createAttribute(String, String)
	 */
	void setAttributeValue(String subjectLogin, String attributeName,
			Object attributeValue) throws ConnectException,
			AttributeNotFoundException;

	/**
	 * Gives back the attribute value of an attribute. We return an
	 * {@link Attribute} object to be able to make a distinction between a
	 * missing attribute and a <code>null</code> attribute value.
	 * 
	 * @param <Type>
	 *            the type of the attribute value.
	 * @param subjectLogin
	 * @param attributeName
	 * @param valueClass
	 *            the type of the attribute value.
	 * @return
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 * @throws RequestDeniedException
	 * @throws SubjectNotFoundException
	 */
	<Type> Attribute<Type> getAttributeValue(String subjectLogin,
			String attributeName, Class<Type> valueClass)
			throws ConnectException, RequestDeniedException,
			SubjectNotFoundException;

	/**
	 * Creates a new (empty) attribute for the given subject.
	 * 
	 * @param subjectLogin
	 * @param attributeName
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 */
	void createAttribute(String subjectLogin, String attributeName,
			Object objectValue) throws ConnectException;

	/**
	 * Removes an attribute for the given subject.
	 * 
	 * @param subjectLogin
	 *            the subject from which to remove the attribute.
	 * @param attributeName
	 *            the name of the attribute to be removed.
	 * @param attributeId
	 *            the optional attributeId in case of a compounded attribute.
	 * @throws ConnectException
	 *             in case the service could not be contacted. Can happen if the
	 *             SSL was not setup correctly.
	 */
	void removeAttribute(String subjectLogin, String attributeName,
			String attributeId) throws ConnectException;

	/**
	 * Removes an attribute.
	 * 
	 * @param <Type>
	 * @param subjectLogin
	 * @param attribute
	 * @throws ConnectException
	 */
	<Type> void removeAttribute(String subjectLogin, Attribute<Type> attribute)
			throws ConnectException;
}
