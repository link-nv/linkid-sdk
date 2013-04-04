/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import com.google.common.base.Function;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.configuration.Protocol;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.util.exception.ValidationFailedException;
import org.jetbrains.annotations.Nullable;


/**
 * Manager class for the stateful protocol handlers.
 * <p/>
 * <p> The state is preserved using the HTTP session. </p>
 *
 * @author fcorneli
 */
public abstract class ProtocolManager {

    private static final Logger logger = Logger.get( ProtocolManager.class );

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
     * @see ProtocolHandler#sendAuthnRequest(HttpServletResponse, AuthenticationContext)
     */
    public static AuthnProtocolRequestContext initiateAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                                     AuthenticationContext context)
            throws IOException {

        ProtocolHandler protocolHandler = getProtocolHandler( context.getProtocol() );
        AuthnProtocolRequestContext authnRequest = protocolHandler.sendAuthnRequest( response, context );

        ProtocolContext.addContext( request.getSession(), authnRequest );
        return authnRequest;
    }

    @Nullable
    public static AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request,
                                                                            final Function<AuthnProtocolResponseContext, AuthenticationContext> responseContext)
            throws ValidationFailedException {

        Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
        for (ProtocolContext protocolContext : contexts.values()) {
            if (protocolContext instanceof AuthnProtocolRequestContext) {
                AuthnProtocolRequestContext protocolRequestContext = (AuthnProtocolRequestContext) protocolContext;
                ProtocolHandler protocolHandler = protocolRequestContext.getProtocolHandler();

                AuthnProtocolResponseContext authnResponse = protocolHandler.findAndValidateAuthnResponse( request, responseContext );
                if (authnResponse != null)
                    return authnResponse;
            }
        }

        logger.dbg( "No authn response found in request matching known Ids." );
        logger.dbg( "Known Contexts:" );
        for (Map.Entry<String, ProtocolContext> protocolContextEntry : contexts.entrySet())
            logger.dbg( "%s: %s", protocolContextEntry.getKey(), protocolContextEntry.getValue() );

        return null;
    }

    @Nullable
    public static AuthnProtocolResponseContext findAndValidateAuthnAssertion(HttpServletRequest request,
                                                                             final Function<AuthnProtocolResponseContext, AuthenticationContext> responseContext)
            throws ValidationFailedException {

        for (Protocol protocol : Protocol.values()) {
            ProtocolHandler protocolHandler = findProtocolHandler( protocol );
            if (null != protocolHandler) {
                AuthnProtocolResponseContext authnResponse = protocolHandler.findAndValidateAuthnAssertion( request, responseContext );
                if (authnResponse != null)
                    return authnResponse;
            }
        }

        logger.dbg( "No authn assertion found in request." );
        return null;
    }

    /**
     * Initiates a logout request.
     *
     * @param request  HTTP servlet request
     * @param response HTTP servlet response
     * @param userId   user ID to logout
     * @param context  logout context
     *
     * @return logout protocol request context
     *
     * @throws IOException something went wrong sending the logout request
     */
    public static LogoutProtocolRequestContext initiateLogout(HttpServletRequest request, HttpServletResponse response, String userId,
                                                              LogoutContext context)
            throws IOException {

        // Delegate the authentication initiation to the relevant protocol handler.
        ProtocolHandler protocolHandler = getProtocolHandler( context.getProtocol() );
        LogoutProtocolRequestContext logoutRequest = protocolHandler.sendLogoutRequest( response, userId, context );

        ProtocolContext.addContext( request.getSession(), logoutRequest );
        return logoutRequest;
    }

    @Nullable
    public static LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                            Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        for (Protocol protocol : Protocol.values()) {
            ProtocolHandler protocolHandler = findProtocolHandler( protocol );
            if (null != protocolHandler) {
                LogoutProtocolRequestContext logoutRequest = protocolHandler.findAndValidateLogoutRequest( request, requestToContext );
                if (logoutRequest != null)
                    return logoutRequest;
            }
        }

        return null;
    }

    @Nullable
    public static LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
            throws ValidationFailedException {

        Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
        for (ProtocolContext protocolContext : contexts.values()) {
            if (protocolContext instanceof LogoutProtocolRequestContext) {
                LogoutProtocolRequestContext protocolRequestContext = (LogoutProtocolRequestContext) protocolContext;
                ProtocolHandler protocolHandler = protocolRequestContext.getProtocolHandler();

                LogoutProtocolResponseContext logoutResponse = protocolHandler.findAndValidateLogoutResponse( request );
                if (logoutResponse != null)
                    return logoutResponse;
            }
        }

        return null;
    }

    private static ProtocolHandler getProtocolHandler(final Protocol protocol) {

        Object protocolHandlerObject = protocol.newHandler();
        if (null == protocolHandlerObject) {
            throw new InternalInconsistencyException(
                    String.format( "Protocol handler not found for protocol %s (class=%s)", protocol.name(), protocol.getProtocolHandlerClass() ) );
        }
        return (ProtocolHandler) protocolHandlerObject;
    }

    @Nullable
    private static ProtocolHandler findProtocolHandler(final Protocol protocol) {

        Object protocolHandlerObject = protocol.newHandler();
        return (ProtocolHandler) protocolHandlerObject;
    }
}
