/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.data.client;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * Interface for data client component. Via this interface application can perform CRUD operations on attributes of a subject. For this the
 * application must be an attribute provider. Only the operator can set the attribute provider role for applications.
 *
 * @author fcorneli
 */
public interface DataClient {

    /**
     * Sets an attribute for specified user. If not yet created will do so.
     *
     * @param attribute the {@link AttributeSDK} to be set.
     * @param userId    userId of subject to set the attribute for
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void setAttributeValue(String userId, AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException;

    /**
     * Sets multiple attributs for specified user. If not yet created will do so.
     *
     * @param attributes the {@link AttributeSDK}'s to be set.
     * @param userId     userId of subject to set the attribute's for
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute or another access violation
     */
    void setAttributeValue(String userId, List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException;

    /**
     * Gives back all attributes for specified user and attribute name.
     *
     * @param userId        the user to query attributes for.
     * @param attributeName the attribute to query
     *
     * @return {@link List} of {@link AttributeSDK} objects containing the request attribute values.
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute or another access violation
     */
    <T extends Serializable> List<AttributeSDK<T>> getAttributes(String userId, String attributeName)
            throws WSClientTransportException, RequestDeniedException;

    /**
     * Creates a new attribute for the given subject.
     *
     * @param attribute the {@link AttributeSDK} to be created.
     * @param userId    userId of subject to set the attribute for
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void createAttribute(String userId, AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException;

    /**
     * Creates a list of new attributes for the given subject.
     *
     * @param attributes the {@link AttributeSDK}'s to be created.
     * @param userId     userId of subject to set the attributes for
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void createAttributes(String userId, List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException;

    /**
     * Removes an attribute's values for the given subject.
     *
     * @param userId        the subject from which to remove the attribute.
     * @param attributeName the name of the attribute to be removed.
     *
     * @throws WSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void removeAttributes(String userId, String attributeName)
            throws WSClientTransportException, RequestDeniedException;

    void removeAttribute(String userId, String attributeName, String attributeId)
            throws WSClientTransportException, RequestDeniedException;

    void removeAttribute(String userId, AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException;

    void removeAttributes(String userId, List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException;
}
