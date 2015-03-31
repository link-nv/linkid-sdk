/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.data.client;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.exception.LinkIDRequestDeniedException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;


/**
 * Interface for data client component. Via this interface application can perform CRUD operations on attributes of a subject. For this the
 * application must be an attribute provider. Only the operator can set the attribute provider role for applications.
 *
 * @author fcorneli
 */
public interface LinkIDDataClient {

    /**
     * Sets an attribute for specified user. If not yet created will do so.
     *
     * @param attribute the {@link LinkIDAttribute} to be set.
     * @param userId    userId of subject to set the attribute for
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void setAttributeValue(String userId, LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    /**
     * Sets multiple attributs for specified user. If not yet created will do so.
     *
     * @param attributes the {@link LinkIDAttribute}'s to be set.
     * @param userId     userId of subject to set the attribute's for
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute or another access violation
     */
    void setAttributeValue(String userId, List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    /**
     * Gives back all attributes for specified user and attribute name.
     *
     * @param userId        the user to query attributes for.
     * @param attributeName the attribute to query
     *
     * @return {@link List} of {@link LinkIDAttribute} objects containing the request attribute values.
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute or another access violation
     */
    <T extends Serializable> List<LinkIDAttribute<T>> getAttributes(String userId, String attributeName)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    /**
     * Creates a new attribute for the given subject.
     *
     * @param attribute the {@link LinkIDAttribute} to be created.
     * @param userId    userId of subject to set the attribute for
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void createAttribute(String userId, LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    /**
     * Creates a list of new attributes for the given subject.
     *
     * @param attributes the {@link LinkIDAttribute}'s to be created.
     * @param userId     userId of subject to set the attributes for
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void createAttributes(String userId, List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    /**
     * Removes an attribute's values for the given subject.
     *
     * @param userId        the subject from which to remove the attribute.
     * @param attributeName the name of the attribute to be removed.
     *
     * @throws LinkIDWSClientTransportException in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws LinkIDRequestDeniedException     in case the application is not allowed write access to the attribute
     */
    void removeAttributes(String userId, String attributeName)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    void removeAttribute(String userId, String attributeName, String attributeId)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    void removeAttribute(String userId, LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;

    void removeAttributes(String userId, List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException;
}
