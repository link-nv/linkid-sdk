/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.idmapping;

import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * Identifier Mapping Service Client interface.
 *
 * The calling application must have explicit permission to use the name identifier mapping web service. This permission can be granted by
 * the operator.
 *
 * @author fcorneli
 */
public interface NameIdentifierMappingClient {

    /**
     * Gives back the user Id corresponding with the given attribute type and identifier.
     *
     * @param attributeType the attribute type.
     * @param identifier the identifier for the attribute type
     *
     * @return the user Id corresponding with the user name.
     *
     * @throws SubjectNotFoundException in case no subject was found.
     * @throws RequestDeniedException in case the calling application has no permission to use the name identifier mapping service.
     * @throws WSClientTransportException in case the name identifier mapping service could not be reached.
     */
    String getUserId(String attributeType, String identifier)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException;
}
