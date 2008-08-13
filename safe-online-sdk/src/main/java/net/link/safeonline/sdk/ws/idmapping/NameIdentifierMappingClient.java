/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Identifier Mapping Service Client interface.
 *
 * The calling application must have explicit permission to use the name identifier mapping web service. This permission
 * can be granted by the operator.
 *
 * @author fcorneli
 *
 */
public interface NameIdentifierMappingClient extends MessageAccessor {

    /**
     * Gives back the user Id corresponding with the given username.
     *
     * @param username
     *            the user name.
     * @return the user Id corresponding with the user name.
     * @throws SubjectNotFoundException
     *             in case no subject was found for the given username.
     * @throws RequestDeniedException
     *             in case the calling application has no permission to use the name identifier mapping service.
     * @throws WSClientTransportException
     *             in case the name identifier mapping service could not be reached.
     */
    String getUserId(String username) throws SubjectNotFoundException, RequestDeniedException,
            WSClientTransportException;
}
