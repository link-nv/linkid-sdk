/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.attrib;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.exception.*;


/**
 * Interface for attribute client. Via components implementing this interface applications can retrieve attributes for subjects.
 * Applications can only retrieve attribute values for which the user confirmed the corresponding application identity.
 * <p/>
 * <p> The attribute value can be of type String, Boolean or an array of Object[] in case of a multivalued attribute. </p>
 *
 * @author fcorneli
 */
public interface LinkIDAttributeClient {

    /**
     * Gives back attribute values via the map of attributes. The map should hold the requested attribute names as keys. The method will
     * fill in the corresponding values.
     */
    void getAttributes(String userId, Map<String, List<LinkIDAttribute<Serializable>>> attributes)
            throws LinkIDAttributeNotFoundException, LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException;

    /**
     * Gives back a map of attributes for the given subject that this application is allowed to read.
     */
    Map<String, List<LinkIDAttribute<Serializable>>> getAttributes(String userId)
            throws LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeNotFoundException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException;

    /**
     * Gives back a list of attributes for the given subject and attribute type that this application is allowed to read.
     */
    <T extends Serializable> List<LinkIDAttribute<T>> getAttributes(String userId, String attributeName)
            throws LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeNotFoundException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException;
}
