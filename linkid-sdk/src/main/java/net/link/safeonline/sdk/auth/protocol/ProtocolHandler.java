/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import java.io.IOException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.Protocol;
import net.link.util.exception.ValidationFailedException;
import org.jetbrains.annotations.Nullable;


/**
 * Interface for protocol handlers. Protocol handlers are stateful since they must be capable of handling the challenge-response aspect of
 * the authentication protocol. Since protocol handlers are stored in the HTTP session they must be serializable.
 *
 * @author fcorneli
 */
public interface ProtocolHandler extends Serializable {

    Protocol getProtocol();

    /**
     * Initiates the authentication request towards the SafeOnline authentication web application.
     *
     * @param response the servlet response.
     * @param context  authentication context containing optional device policy, ...
     *
     * @return Authentication protocol request context
     *
     * @throws IOException The request could not be written to the response.
     */
    AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException;

    /**
     * Finalize the active authentication process.
     *
     * @param request HTTP Servlet Request
     *
     * @return Details about the authentication such as the authenticated user's application identifier or <code>null</code> if the handler
     * thinks the request has nothing to do with authentication.
     *
     * @throws ValidationFailedException Validation failed for the incoming authentication response.
     */
    @Nullable
    AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException;

    /**
     * Complete a detached (without request) authentication.
     *
     * @return Details about the authentication such as the authenticated user's application identifier or <code>null</code> if the handler
     * finds no detached authentication assertion in the request.
     */
    @Nullable
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(HttpServletRequest request)
            throws ValidationFailedException;
}
