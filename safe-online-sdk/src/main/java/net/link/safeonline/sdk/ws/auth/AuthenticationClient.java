/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.PublicKey;

import javax.xml.datatype.DatatypeConfigurationException;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Interface for authentication client. Via components implementing this interface applications can authenticate against OLAS. *
 * 
 * @author wvdhaute
 * 
 */
public interface AuthenticationClient extends MessageAccessor {

    /**
     * Authenticates for the specified application, using the specified device, given the specified device credentials.
     * 
     * 
     * @param applicationId
     * @param deviceName
     * @param deviceCredentials
     * @param publicKey
     * 
     * @throws RequestDeniedException
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws DatatypeConfigurationException
     */
    void authenticate(String applicationId, String deviceName, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, WSClientTransportException, DatatypeConfigurationException;
}
