/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.openid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.configuration.*;
import net.link.util.exception.ValidationFailedException;
import net.link.util.ssl.CustomSSLSocketFactory;
import net.link.util.ssl.X509CertificateTrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.jetbrains.annotations.Nullable;
import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.*;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.xri.XriResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcherFactory;


/**
 * Implementation class for the SAML2 browser POST authentication protocol.
 * <p/>
 * <p> Optional configuration parameters: </p> <ul> </ul>
 * <p/>
 * <p> Optional session configuration attributes: </p> <ul> </ul>
 *
 * @author fcorneli
 */
public class OpenIdProtocolHandler implements ProtocolHandler {

    private static final Logger logger = Logger.get( OpenIdProtocolHandler.class );

    /*
    * Consumer Manager cannot be included in the OpenIDContext as it is not serializable.
    */
    private static ConsumerManager manager;

    static {
        try {
            Message.addExtensionFactory( AuthenticatedDevicesMessage.class );
            Message.addExtensionFactory( UserInterfaceMessage.class );
        }
        catch (MessageException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    private DiscoveryInformation  discovered;
    private AuthenticationContext authnContext;

    @Override
    public Protocol getProtocol() {

        return Protocol.OPENID;
    }

    @Override
    public AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException {

        authnContext = context;

        String realm = getRealm();
        logger.dbg( "realm: %s", realm );
        String discoveryUrl = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().openid().discoveryPath() );
        logger.dbg( "discoveryUrl: %s", discoveryUrl );

        String targetURL = context.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
        logger.dbg( "target url: %s", targetURL );

        String landingURL = null;
        if (config().web().landingPath() != null)
            landingURL = ConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
        logger.dbg( "landing url: %s", landingURL );

        if (landingURL == null) {
            // If no landing URL is configured, land on target.
            landingURL = targetURL;
        }

        try {
            ConsumerManager manager = getManager( context.getOpenID().getSslCertificate() );

            @SuppressWarnings("unchecked")
            List<DiscoveryInformation> discoveries = manager.discover( discoveryUrl );
            discovered = manager.associate( discoveries );

            AuthRequest authReq = manager.authenticate( discovered, landingURL, realm );
            response.sendRedirect( authReq.getDestinationUrl( true ) );

            return new AuthnProtocolRequestContext( realm, realm, this, targetURL, context.isMobileAuthentication(),
                    context.isMobileAuthenticationMinimal() );
        }
        catch (OpenIDException e) {
            logger.err( "OpenID OpenIDException", e );
            throw new InternalInconsistencyException( e );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        ParameterList parameterList = new ParameterList( request.getParameterMap() );

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty())
            receivingURL.append( '?' ).append( request.getQueryString() );

        // debug( parameterList );

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification;
        try {
            verification = getManager( authnContext.getOpenID().getSslCertificate() ).verify( receivingURL.toString(), parameterList, discovered );
            logger.dbg( "discovered: %s", discovered.getOPEndpoint().toString() );
            logger.dbg( "verification: %s", verification.getStatusMsg() );
        }
        catch (OpenIDException e) {
            logger.err( "OpenID OpenIDException", e );
            throw new InternalInconsistencyException( e );
        }

        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        List<String> authenticatedDevices = new LinkedList<String>();
        Message authResponse = verification.getAuthResponse();
        Identifier identifier = verification.getVerifiedId();
        String userId = null;
        if (identifier != null) {
            userId = identifier.getIdentifier();
            logger.dbg( "userId: %s", userId );

            // attribute exchange support
            if (authResponse.hasExtension( AxMessage.OPENID_NS_AX ))
                try {
                    FetchResponse fetchResp = (FetchResponse) authResponse.getExtension( AxMessage.OPENID_NS_AX );
                    attributes = OpenIdUtil.getAttributeMap( fetchResp );
                }
                catch (MessageException e) {
                    logger.err( "OpenID MessageException", e );
                    throw new InternalInconsistencyException( e );
                }

            // authenticated devices support
            if (authResponse.hasExtension( AuthenticatedDevicesMessage.LINKID_AUTH_DEVICES_NS ))
                try {
                    AuthenticatedDevicesMessage authDevicesMessage = (AuthenticatedDevicesMessage) authResponse.getExtension(
                            AuthenticatedDevicesMessage.LINKID_AUTH_DEVICES_NS );
                    Iterables.addAll( authenticatedDevices, authDevicesMessage );
                }
                catch (MessageException e) {
                    logger.err( "OpenID MessageException", e );
                    throw new InternalInconsistencyException( e );
                }
        }

        String realm = getRealm();
        AuthnProtocolRequestContext requestContext = ProtocolContext.findContext( request.getSession(), realm );
        if (null == requestContext) {
            throw new ValidationFailedException( "OpenID response returned but no matching request found?!" );
        }

        boolean success = verification.getAuthResponse() instanceof AuthSuccess;
        return new AuthnProtocolResponseContext( requestContext, realm, userId, requestContext.getIssuer(), authenticatedDevices, attributes, success,
                null );
    }

    @Nullable
    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        logger.dbg( "OpenID implementation does not support detached authentication yet" );
        return null;
    }

    @Override
    public LogoutProtocolRequestContext sendLogoutRequest(HttpServletResponse response, String userId, LogoutContext context)
            throws IOException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    @Override
    public LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
            throws ValidationFailedException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    @Nullable
    @Override
    public LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                     Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        // not supported by OpenID
        return null;
    }

    @Override
    public LogoutProtocolResponseContext sendLogoutResponse(HttpServletResponse response, LogoutProtocolRequestContext logoutRequestContext,
                                                            boolean partialLogout)
            throws IOException {

        throw new UnsupportedOperationException( "OpenID implementation does not support single logout" );
    }

    @SuppressWarnings("unused")
    private void debug(final ParameterList parameterList) {

        ConsumerManager manager = getManager( authnContext.getOpenID().getSslCertificate() );

        // DEBUG
        try {
            AuthSuccess authResp = AuthSuccess.createAuthSuccess( parameterList );
            Identifier respClaimed = manager.getDiscovery().parseIdentifier( authResp.getClaimed(), true );
            @SuppressWarnings("unchecked")
            List<DiscoveryInformation> discoveries = manager.getDiscovery().discover( respClaimed );

            logger.dbg( "discovered" );
            logger.dbg( "  * getDelegateIdentifier(): %s", discovered.getDelegateIdentifier() );
            logger.dbg( "  * getClaimedIdentifier(): %s", discovered.getClaimedIdentifier() );
            logger.dbg( "  * getClaimedIdentifier.getVersion(): %s", discovered.getVersion() );
            logger.dbg( "  * getOPEndpoint(): %s", discovered.getOPEndpoint() );

            for (DiscoveryInformation discovery : discoveries) {
                logger.dbg( "service" );
                logger.dbg( "  * getDelegateIdentifier(): %s", discovery.getDelegateIdentifier() );
                logger.dbg( "  * getClaimedIdentifier(): %s", discovery.getClaimedIdentifier() );
                logger.dbg( "  * getClaimedIdentifier.getVersion(): %s", discovery.getVersion() );
                logger.dbg( "  * getOPEndpoint(): %s", discovery.getOPEndpoint() );
            }

            logger.dbg( "response" );
            logger.dbg( "  * assertId: %s", authResp.getIdentity() );
            logger.dbg( "  * respEndpoint: %s", authResp.getOpEndpoint() );
            logger.dbg( "  * respClaimed: %s", respClaimed );
            logger.dbg( "  * handle: %s", authResp.getHandle() );

            Association association = manager.getAssociations().load( discovered.getOPEndpoint().toString(), authResp.getHandle() );
            if (null != association)
                logger.dbg( "found association: %s", association.getHandle() );
            else
                logger.dbg( "association %s not found.", authResp.getHandle() );
        }
        catch (MessageException e) {
            logger.err( e, "MessageException" );
        }
        catch (DiscoveryException e) {
            logger.err( e, "[TODO]" );
        }
        // END-DEBUG
    }

    private static String getRealm() {

        String realm = config().proto().openid().realm();
        if (realm == null)
            realm = ConfigUtils.getApplicationConfidentialURL();
        return realm;
    }

    public ConsumerManager getManager(final X509Certificate sslCertificate) {

        if (null == manager) {

            // ConsumerManager initialization
            try {

                TrustManager trustManager;
                if (null == sslCertificate) {
                    CustomSSLSocketFactory.installAllTrusted();
                    trustManager = new X509CertificateTrustManager();
                } else {
                    CustomSSLSocketFactory.install( sslCertificate );
                    trustManager = new X509CertificateTrustManager( sslCertificate );
                }

                SSLContext sslContext = SSLContext.getInstance( "SSL" );
                TrustManager[] trustManagers = { trustManager };
                sslContext.init( null, trustManagers, null );
                HttpFetcherFactory httpFetcherFactory = new HttpFetcherFactory( sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
                YadisResolver yadisResolver = new YadisResolver( httpFetcherFactory );
                RealmVerifierFactory realmFactory = new RealmVerifierFactory( yadisResolver );
                HtmlResolver htmlResolver = new HtmlResolver( httpFetcherFactory );
                XriResolver xriResolver = Discovery.getXriResolver();
                Discovery discovery = new Discovery( htmlResolver, yadisResolver, xriResolver );
                manager = new ConsumerManager( realmFactory, discovery, httpFetcherFactory );
            }
            catch (KeyManagementException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (NoSuchAlgorithmException e) {
                throw new InternalInconsistencyException( e );
            }
            catch (KeyStoreException e) {
                throw new InternalInconsistencyException( e );
            }
        }
        return manager;
    }
}
