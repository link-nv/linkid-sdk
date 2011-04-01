/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.logging.exception.*;


/**
 * Interface for attribute client. Via components implementing this interface applications can retrieve attributes for subjects.
 * Applications can only retrieve attribute values for which the user confirmed the corresponding application identity.
 *
 * <p> The attribute value can be of type String, Boolean or an array of Object[] in case of a multivalued attribute. </p>
 *
 * @author fcorneli
 */
public interface AttributeClient {

    /**
     * Gives back attribute values via the map of attributes. The map should hold the requested attribute names as keys. The method will
     * fill in the corresponding values.
     *
     * @throws AttributeNotFoundException
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    void getAttributes(String userId, Map<String, List<AttributeSDK<?>>> attributes)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException,
                   SubjectNotFoundException;

    /**
     * Gives back a map of attributes for the given subject that this application is allowed to read.
     *
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     * @throws AttributeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    Map<String, List<AttributeSDK<?>>> getAttributes(String userId)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException;

    /**
     * Gives back a list of attributes for the given subject and attribute type that this application is allowed to read.
     *
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     * @throws AttributeNotFoundException
     * @throws AttributeUnavailableException
     * @throws SubjectNotFoundException
     */
    <T extends Serializable> List<AttributeSDK<T>> getAttributes(String userId, String attributeName)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException;
}
