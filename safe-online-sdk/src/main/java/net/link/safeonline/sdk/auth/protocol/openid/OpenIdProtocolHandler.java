/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.openid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.util.error.ValidationFailedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;


/**
 * Implementation class for the SAML2 browser POST authentication protocol.
 *
 * <p> Optional configuration parameters: </p> <ul> </ul>
 *
 * <p> Optional session configuration attributes: </p> <ul> </ul>
 *
 * @author fcorneli
 */
public class OpenIdProtocolHandler implements ProtocolHandler {

    private static final Log LOG = LogFactory.getLog( OpenIdProtocolHandler.class );

    static {
        try {
            Message.addExtensionFactory( AuthenticatedDevicesMessage.class );
        }
        catch (MessageException e) {
            throw new RuntimeException( e );
        }
    }

    private DiscoveryInformation  discovered;
    private AuthenticationContext authnContext;

    public AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException {

        authnContext = context;

        String realm = config().proto().openID().realm();
        if (realm == null)
            realm = ConfigUtils.getApplicationConfidentialURL();
        LOG.debug( "realm: " + realm );
        String discoveryUrl = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().openID().discoveryPath() );
        LOG.debug( "discoveryUrl: " + discoveryUrl );

        String targetURL = context.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
        LOG.debug( "target url: " + targetURL );

        String landingURL = null;
        if (config().web().landingPath() != null)
            landingURL = ConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
        LOG.debug( "landing url: " + landingURL );

        if (landingURL == null) {
            // If no landing URL is configured, land on target.
            landingURL = targetURL;
            targetURL = null;
        }

        try {
            ConsumerManager manager = context.getOpenID().getManager();

            @SuppressWarnings( { "unchecked" })
            List<DiscoveryInformation> discoveries = manager.discover( discoveryUrl );
            discovered = manager.associate( discoveries );

            AuthRequest authReq = manager.authenticate( discovered, landingURL, realm );
            response.sendRedirect( authReq.getDestinationUrl( true ) );

            return new AuthnProtocolRequestContext( authReq.getHandle(), null, this, targetURL );
        }
        catch (OpenIDException e) {
            LOG.error( "OpenID OpenIDException", e );
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings("unchecked")
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        ParameterList parameterList = new ParameterList( request.getParameterMap() );

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append( '?' ).append( request.getQueryString() );

        // debug( parameterList );

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification;
        try {
            verification = authnContext.getOpenID().getManager().verify( receivingURL.toString(), parameterList, discovered );
            LOG.debug( "discovered: " + discovered.getOPEndpoint().toString() );
            LOG.debug( "verification: " + verification.getStatusMsg() );
        }
        catch (OpenIDException e) {
            LOG.error( "OpenID OpenIDException", e );
            throw new RuntimeException( e );
        }

        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        List<String> authenticatedDevices = new LinkedList<String>();
        Message authResponse = verification.getAuthResponse();
        Identifier identifier = verification.getVerifiedId();
        String userId = null;
        if (identifier != null) {
            userId = identifier.getIdentifier();
            LOG.debug( "userId: " + userId );

            // attribute exchange support
            if (authResponse.hasExtension( AxMessage.OPENID_NS_AX ))
                try {
                    FetchResponse fetchResp = (FetchResponse) authResponse.getExtension( AxMessage.OPENID_NS_AX );
                    attributes = OpenIdUtil.getAttributeMap( fetchResp );
                }
                catch (MessageException e) {
                    LOG.error( "OpenID MessageException", e );
                    throw new RuntimeException( e );
                }

            // authenticated devices support
            if (authResponse.hasExtension( AuthenticatedDevicesMessage.LINKID_AUTH_DEVICES_NS ))
                try {
                    AuthenticatedDevicesMessage authDevicesMessage = (AuthenticatedDevicesMessage) authResponse.getExtension(
                            AuthenticatedDevicesMessage.LINKID_AUTH_DEVICES_NS );
                    Iterables.addAll( authenticatedDevices, authDevicesMessage );
                }
                catch (MessageException e) {
                    LOG.error( "OpenID MessageException", e );
                    throw new RuntimeException( e );
                }
        }

        String handle = null;
        AuthnProtocolRequestContext requestContext = null;
        if (authResponse instanceof AuthSuccess) {
            handle = ((AuthSuccess) authResponse).getHandle();
            requestContext = ProtocolContext.findContext( request.getSession(), handle );
        }

        boolean success = verification.getAuthResponse() instanceof AuthSuccess;
        return new AuthnProtocolResponseContext( requestContext, handle, userId, requestContext == null? null: requestContext.getIssuer(),
                authenticatedDevices, attributes, success, null );
    }

    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        LOG.debug( "OpenID implementation does not support detached authentication yet" );
        return null;
    }

    public LogoutProtocolRequestContext sendLogoutRequest(HttpServletResponse response, String userId, LogoutContext context)
            throws IOException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    public LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
            throws ValidationFailedException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    public LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                     Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    public LogoutProtocolResponseContext sendLogoutResponse(HttpServletResponse response, LogoutProtocolRequestContext logoutRequestContext,
                                                            boolean partialLogout)
            throws IOException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    @SuppressWarnings("unused")
    private void debug(final ParameterList parameterList) {

        ConsumerManager manager = authnContext.getOpenID().getManager();

        // DEBUG
        try {
            AuthSuccess authResp = AuthSuccess.createAuthSuccess( parameterList );
            Identifier respClaimed = manager.getDiscovery().parseIdentifier( authResp.getClaimed(), true );
            @SuppressWarnings("unchecked")
            List<DiscoveryInformation> discoveries = manager.getDiscovery().discover( respClaimed );

            LOG.debug( "discovered" );
            LOG.debug( "  * getDelegateIdentifier(): " + discovered.getDelegateIdentifier() );
            LOG.debug( "  * getClaimedIdentifier(): " + discovered.getClaimedIdentifier() );
            LOG.debug( "  * getClaimedIdentifier.getVersion(): " + discovered.getVersion() );
            LOG.debug( "  * getOPEndpoint(): " + discovered.getOPEndpoint() );

            for (DiscoveryInformation discovery : discoveries) {
                LOG.debug( "service" );
                LOG.debug( "  * getDelegateIdentifier(): " + discovery.getDelegateIdentifier() );
                LOG.debug( "  * getClaimedIdentifier(): " + discovery.getClaimedIdentifier() );
                LOG.debug( "  * getClaimedIdentifier.getVersion(): " + discovery.getVersion() );
                LOG.debug( "  * getOPEndpoint(): " + discovery.getOPEndpoint() );
            }

            LOG.debug( "response" );
            LOG.debug( "  * assertId: " + authResp.getIdentity() );
            LOG.debug( "  * respEndpoint: " + authResp.getOpEndpoint() );
            LOG.debug( "  * respClaimed: " + respClaimed );
            LOG.debug( "  * handle: " + authResp.getHandle() );

            Association association = manager.getAssociations().load( discovered.getOPEndpoint().toString(), authResp.getHandle() );
            if (null != association)
                LOG.debug( "found association: " + association.getHandle() );
            else
                LOG.debug( "association " + authResp.getHandle() + " not found" );
        }
        catch (MessageException e) {
            LOG.error( "MessageException", e );
        }
        catch (DiscoveryException e) {
            LOG.error( "[TODO]", e );
        }
        // END-DEBUG
    }
}
