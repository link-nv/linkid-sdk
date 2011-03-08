/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;


/**
 * Implementation class for the SAML2 browser POST authentication protocol.
 *
 * @author fcorneli
 */
public class Saml2ProtocolHandler implements ProtocolHandler {

    private static final Log LOG = LogFactory.getLog( Saml2ProtocolHandler.class );

    static {
        // Because Sun loves to endorse crippled versions of Xerces.
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                            "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library", e );
        }
    }

    private AuthenticationContext authnContext;
    private LogoutContext         logoutContext;

    /**
     * {@inheritDoc}
     */
    public AuthnProtocolRequestContext sendAuthnRequest(HttpServletResponse response, AuthenticationContext context)
            throws IOException {

        authnContext = context;

        String targetURL = authnContext.getTarget();
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

        String authnService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().authPath() );

        String templateResourceName = config().proto().saml().postBindingTemplate();

        AuthnRequest samlRequest = AuthnRequestFactory.createAuthnRequest( authnContext.getApplicationName(), null,
                                                                           authnContext.getApplicationFriendlyName(), landingURL,
                                                                           authnService, authnContext.getDevices(),
                                                                           authnContext.isForceAuthentication(),
                                                                           authnContext.getSessionTrackingId() );

        List<X509Certificate> certificateChain = null;
        if (null != authnContext.getApplicationCertificate()) {
            certificateChain = Collections.singletonList( authnContext.getApplicationCertificate() );
        }

        RequestUtil.sendRequest( authnService, authnContext.getSAML().getBinding(), samlRequest, authnContext.getApplicationKeyPair(),
                                 certificateChain, response, authnContext.getSAML().getRelayState(), templateResourceName,
                                 authnContext.getLanguage(), authnContext.getThemeName() );

        LOG.debug( "sending Authn Request for: " + authnContext.getApplicationName() + ", issuer: " + samlRequest.getIssuer().getValue() );
        return new AuthnProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, targetURL );
    }

    /**
     * {@inheritDoc}
     */
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(HttpServletRequest request)
            throws ValidationFailedException {

        if (authnContext == null)
            // This protocol handler has not sent an authentication request.
            return null;

        Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
        Saml2ResponseContext saml2ResponseContext = ResponseUtil.findAndValidateAuthnResponse( request, contexts,
                                                                                               authnContext.getApplicationCertificate(),
                                                                                               authnContext.getApplicationKeyPair()
                                                                                                       .getPrivate(),
                                                                                               authnContext.getServiceCertificates(),
                                                                                               authnContext.getServiceRootCertificates() );
        if (null == saml2ResponseContext)
            // The request does not contain an authentication response or it didn't match the request sent by this protocol handler.
            return null;

        Response samlResponse = (Response) saml2ResponseContext.getResponse();

        String userId = null;
        List<String> authenticatedDevices = new LinkedList<String>();
        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        if (!samlResponse.getAssertions().isEmpty()) {
            Assertion assertion = samlResponse.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            for (AuthnStatement authnStatement : assertion.getAuthnStatements()) {
                authenticatedDevices.add( authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
                LOG.debug( "authenticated device " + authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef() );
            }
            attributes.putAll( Saml2Util.getAttributeValues( assertion ) );
        }
        LOG.debug( "userId: " + userId );

        boolean success = samlResponse.getStatus().getStatusCode().getValue().equals( StatusCode.SUCCESS_URI );
        AuthnProtocolRequestContext authnRequest = ProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );
        return new AuthnProtocolResponseContext( authnRequest, samlResponse.getID(), userId, authenticatedDevices, attributes, success,
                                                 saml2ResponseContext.getCertificateChain() );
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolRequestContext sendLogoutRequest(HttpServletResponse response, String userId, LogoutContext context)
            throws IOException {

        logoutContext = context;

        String targetURL = logoutContext.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
        LOG.debug( "target url: " + targetURL );

        String logoutService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().logoutPath() );

        String templateResourceName = config().proto().saml().postBindingTemplate();

        LogoutRequest samlRequest = LogoutRequestFactory.createLogoutRequest( userId, logoutContext.getApplicationName(), logoutService,
                                                                              logoutContext.getSessionTrackingId() );

        List<X509Certificate> certificateChain = null;
        if (null != context.getApplicationCertificate()) {
            certificateChain = Collections.singletonList( context.getApplicationCertificate() );
        }

        RequestUtil.sendRequest( logoutService, logoutContext.getSAML().getBinding(), samlRequest, logoutContext.getApplicationKeyPair(),
                                 certificateChain, response, logoutContext.getSAML().getRelayState(), templateResourceName,
                                 logoutContext.getLanguage(), logoutContext.getThemeName() );

        return new LogoutProtocolRequestContext( samlRequest.getID(), samlRequest.getIssuer().getValue(), this, targetURL,
                                                 samlRequest.getNameID().getValue() );
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolResponseContext findAndValidateLogoutResponse(HttpServletRequest request)
            throws ValidationFailedException {

        if (logoutContext == null)
            // This protocol handler has not sent an authentication request.
            return null;

        Map<String, ProtocolContext> contexts = ProtocolContext.getContexts( request.getSession() );
        Saml2ResponseContext saml2ResponseContext = ResponseUtil.findAndValidateLogoutResponse( request, contexts,
                                                                                                logoutContext.getApplicationCertificate(),
                                                                                                logoutContext.getApplicationKeyPair()
                                                                                                        .getPrivate(),
                                                                                                logoutContext.getServiceCertificates(),
                                                                                                logoutContext.getServiceRootCertificates() );
        if (null == saml2ResponseContext)
            // The request does not contain a logout response or it didn't match the request sent by this protocol handler.
            return null;

        LogoutResponse samlResponse = (LogoutResponse) saml2ResponseContext.getResponse();

        String status = samlResponse.getStatus().getStatusCode().getValue();
        boolean success = !(!status.equals( StatusCode.SUCCESS_URI ) && !status.equals( StatusCode.PARTIAL_LOGOUT_URI ));
        LogoutProtocolRequestContext logoutRequest = ProtocolContext.findContext( request.getSession(), samlResponse.getInResponseTo() );
        return new LogoutProtocolResponseContext( logoutRequest, samlResponse.getID(), success,
                                                  saml2ResponseContext.getCertificateChain() );
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolRequestContext findAndValidateLogoutRequest(HttpServletRequest request,
                                                                     Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        LogoutRequest samlRequest = RequestUtil.findLogoutRequest( request );
        if (null == samlRequest)
            // The request does not contain a logout request of the protocol handled by this protocol handler.
            return null;

        LogoutProtocolRequestContext logoutRequest = new LogoutProtocolRequestContext( samlRequest.getID(),
                                                                                       samlRequest.getIssuer().getValue(), this, null,
                                                                                       samlRequest.getNameID().getValue() );
        logoutContext = requestToContext.apply( logoutRequest );

        List<X509Certificate> certificateChain = RequestUtil.validateRequest( request, samlRequest,
                                                                              logoutContext.getApplicationCertificate(),
                                                                              logoutContext.getApplicationKeyPair().getPrivate(),
                                                                              logoutContext.getServiceCertificates(),
                                                                              logoutContext.getServiceRootCertificates() );

        logoutRequest.setCertificateChain( certificateChain );
        return logoutRequest;
    }

    /**
     * {@inheritDoc}
     */
    public LogoutProtocolResponseContext sendLogoutResponse(HttpServletResponse response, LogoutProtocolRequestContext logoutRequestContext,
                                                            boolean partialLogout)
            throws IOException {

        Preconditions.checkNotNull( logoutContext, "This protocol handler has not received a logout request to respond to." );

        String templateResourceName = config().proto().saml().postBindingTemplate();
        String logoutExitService = ConfigUtils.getLinkIDAuthURLFromPath( config().linkID().logoutExitPath() );

        LogoutResponse samlLogoutResponse = LogoutResponseFactory.createLogoutResponse( partialLogout, logoutRequestContext,
                                                                                        logoutContext.getApplicationName(),
                                                                                        logoutExitService );

        List<X509Certificate> certificateChain = null;
        if (null != logoutContext.getApplicationCertificate()) {
            certificateChain = Collections.singletonList( logoutContext.getApplicationCertificate() );
        }

        ResponseUtil.sendResponse( logoutExitService, logoutContext.getSAML().getBinding(), samlLogoutResponse,
                                   logoutContext.getApplicationKeyPair(), certificateChain, response,
                                   logoutContext.getSAML().getRelayState(), templateResourceName, null );

        String status = samlLogoutResponse.getStatus().getStatusCode().getValue();
        return new LogoutProtocolResponseContext( logoutRequestContext, samlLogoutResponse.getID(), //
                                                  status.equals( StatusCode.SUCCESS_URI ) || status.equals( StatusCode.PARTIAL_LOGOUT_URI ),
                                                  certificateChain );
    }

    @Override
    public String toString() {

        return String.format( "{proto: SAML2, auth=%s, logout=%s}", authnContext, logoutContext );
    }
}
