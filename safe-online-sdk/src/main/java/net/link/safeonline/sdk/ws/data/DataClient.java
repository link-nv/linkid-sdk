/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Interface for data client component. Via this interface application can perform CRUD operations on attributes of a subject. For this the
 * application must be an attribute provider. Only the operator can set the attribute provider role for applications.
 * 
 * @author fcorneli
 * 
 */
public interface DataClient extends MessageAccessor {

    /**
     * Sets the value of an attribute. Please notice that the attribute should already be defined via:
     * {@link #createAttribute(String, String, Object)}. The attribute value can be of type {@link String} or {@link Boolean}.
     * 
     * @param userId
     * @param attributeName
     * @param attributeValue
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws AttributeNotFoundException
     *             in case the attribute entity did not exist.
     * @see #createAttribute(String, String, Object)
     */
    void setAttributeValue(String userId, String attributeName, Object attributeValue) throws WSClientTransportException,
                                                                                      AttributeNotFoundException;

    /**
     * Gives back the attribute value of an attribute. We return an {@link Attribute} object to be able to make a distinction between a
     * missing attribute and a <code>null</code> attribute value.
     * 
     * @param <Type>
     *            the type of the attribute value.
     * @param userId
     * @param attributeName
     * @param valueClass
     *            the type of the attribute value.
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException
     * @throws SubjectNotFoundException
     */
    <Type> Attribute<Type> getAttributeValue(String userId, String attributeName, Class<Type> valueClass)
                                                                                                         throws WSClientTransportException,
                                                                                                         RequestDeniedException,
                                                                                                         SubjectNotFoundException;

    /**
     * Creates a new (empty) attribute for the given subject.
     * 
     * @param userId
     * @param attributeName
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     */
    void createAttribute(String userId, String attributeName, Object objectValue) throws WSClientTransportException;

    /**
     * Removes an attribute for the given subject.
     * 
     * @param userId
     *            the subject from which to remove the attribute.
     * @param attributeName
     *            the name of the attribute to be removed.
     * @param attributeId
     *            the optional attributeId in case of a compounded attribute.
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     */
    void removeAttribute(String userId, String attributeName, String attributeId) throws WSClientTransportException;

    /**
     * Removes an attribute.
     * 
     * @param <Type>
     * @param userId
     * @param attribute
     * @throws WSClientTransportException
     */
    <Type> void removeAttribute(String userId, Attribute<Type> attribute) throws WSClientTransportException;
}
