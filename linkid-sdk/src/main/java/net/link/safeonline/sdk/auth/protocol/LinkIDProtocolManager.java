/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.util.InternalInconsistencyException;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import org.jetbrains.annotations.Nullable;


/**
 * Manager class for the stateful protocol handlers.
 * <p/>
 * <p> The state is preserved using the HTTP session. </p>
 *
 * @author fcorneli
 */
public abstract class LinkIDProtocolManager {

    private static final Logger logger = Logger.get( LinkIDProtocolManager.class );

    /**
     * Initiates the authentication.
     *
     * @param request  HTTP servlet request
     * @param response HTTP servlet response
     * @param context  authentication context
     *
     * @return protocol context, use to finalize then authentication
     *
     * @throws IOException something went wrong sending the authentication request
     * @see LinkIDProtocolHandler#sendAuthnRequest(HttpServletResponse, LinkIDAuthenticationContext)
     */
    public static LinkIDAuthnProtocolRequestContext initiateAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                                           LinkIDAuthenticationContext context)
            throws IOException {

        // !! need to create the HttpSession before sending the authentication request
        // the HAWS commits the response and creating a session after a redirect is not allowed
        // Cannot create a session after the response has been committed
        HttpSession httpSession = request.getSession();
        LinkIDProtocolHandler linkIDProtocolHandler = getProtocolHandler( context.getProtocol() );
        LinkIDAuthnProtocolRequestContext authnRequest = linkIDProtocolHandler.sendAuthnRequest( response, context );

        LinkIDProtocolContext.addContext( httpSession, authnRequest );
        return authnRequest;
    }

    @Nullable
    public static LinkIDAuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        Map<String, LinkIDProtocolContext> contexts = LinkIDProtocolContext.getContexts( request.getSession() );
        for (LinkIDProtocolContext linkIDProtocolContext : contexts.values()) {
            if (linkIDProtocolContext instanceof LinkIDAuthnProtocolRequestContext) {
                LinkIDAuthnProtocolRequestContext protocolRequestContext = (LinkIDAuthnProtocolRequestContext) linkIDProtocolContext;
                LinkIDProtocolHandler linkIDProtocolHandler = protocolRequestContext.getLinkIDProtocolHandler();

                LinkIDAuthnProtocolResponseContext authnResponse = linkIDProtocolHandler.findAndValidateAuthnResponse( request );
                if (authnResponse != null) {
                    return authnResponse;
                }
            }
        }

        logger.dbg( "No authn response found in request matching known Ids." );
        logger.dbg( "Known Contexts:" );
        for (Map.Entry<String, LinkIDProtocolContext> protocolContextEntry : contexts.entrySet()) {
            logger.dbg( "%s: %s", protocolContextEntry.getKey(), protocolContextEntry.getValue() );
        }

        return null;
    }

    private static LinkIDProtocolHandler getProtocolHandler(final LinkIDProtocol protocol) {

        Object protocolHandlerObject = protocol.newHandler();
        if (null == protocolHandlerObject) {
            throw new InternalInconsistencyException(
                    String.format( "Protocol handler not found for protocol %s (class=%s)", protocol.name(), protocol.getProtocolHandlerClass() ) );
        }
        return (LinkIDProtocolHandler) protocolHandlerObject;
    }
}
