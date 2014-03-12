/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import com.google.common.base.Function;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.util.*;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.attribute.*;
import net.link.safeonline.sdk.api.auth.ForceAuth;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.auth.protocol.*;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthInvalidMessageException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.*;
import net.link.safeonline.sdk.configuration.*;
import net.link.util.exception.ValidationFailedException;
import org.joda.time.DateTime;


/**
 * <p/>
 * Date: 09/05/12
 * Time: 14:46
 *
 * @author sgdesmet
 */
@SuppressWarnings("UnusedDeclaration")
public class OAuth2ProtocolHandler implements ProtocolHandler {

    private static final Logger logger = Logger.get( OAuth2ProtocolHandler.class );

    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping()
                                                   .generateNonExecutableJson()
                                                   .setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES )
                                                   .setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    private AuthenticationContext authnContext;

    @Override
    public Protocol getProtocol() {

        return Protocol.OAUTH2;
    }

    @Override
    public AuthnProtocolRequestContext sendAuthnRequest(final HttpServletResponse response, final AuthenticationContext context)
            throws IOException {

        logger.dbg( "send oauth authn request" );

        authnContext = context;

        String targetURL = getTargetUrl();
        String landingURL = getLandingUrl();

        String authnService = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().authorizationPath() );

        String clientId = MessageUtils.stringEmpty( config().proto().oauth2().clientId() )? context.getApplicationName(): config().proto().oauth2().clientId();

        // create oauht2 authorization request ( authorization grant code flow)
        AuthorizationRequest authorizationRequest = new AuthorizationRequest( OAuth2Message.ResponseType.CODE, clientId );
        authorizationRequest.setRedirectUri( landingURL );
        String state = UUID.randomUUID().toString();
        authorizationRequest.setState( state );
        // note: scope is not set in the authorization request, this is preconfigured by the linkid operator
        boolean paramsInBody = config().proto().oauth2().binding().contains( "POST" );
        // add login mode (this is not part of oauth, but linkid)
        List<String> loginParams = new ArrayList<String>( 2 );
        loginParams.add( RequestConstants.LOGINMODE_REQUEST_PARAM );
        loginParams.add( context.getLoginMode().toString() );
        loginParams.add( RequestConstants.OAUTH2_FORCE_AUTHN );
        loginParams.add( authnContext.isForceAuthentication()? ForceAuth.FORCE.toString(): ForceAuth.AUTO.toString() );
        if (null != context.getStartPage()) {
            loginParams.add( RequestConstants.START_PAGE_REQUEST_PARAM );
            loginParams.add( context.getStartPage().name() );
        }

        MessageUtils.sendRedirectMessage( authnService, authorizationRequest, response, paramsInBody, loginParams );

        AuthnProtocolRequestContext requestContext = new AuthnProtocolRequestContext( authorizationRequest.getState(), clientId, this, targetURL,
                context.isMobileAuthentication(), context.isMobileAuthenticationMinimal(), context.isMobileForceRegistration() );
        requestContext.setLoginMode( context.getLoginMode() );
        return requestContext;
    }

    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnResponse(final HttpServletRequest request,
                                                                     final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        logger.dbg( "Find and validate OAuth2 response" );

        if (authnContext == null)
            // This protocol handler has not sent an authentication request.
            return null;

        boolean success;
        // get the message
        ResponseMessage responseMessage;
        try {
            responseMessage = MessageUtils.getAuthorizationCodeResponse( request );
        }
        catch (OAuthInvalidMessageException e) {
            throw new ValidationFailedException( "Unexpected response message: ", e );
        }

        // get state from message
        String state = null;
        if (responseMessage instanceof ErrorResponse) {
            state = ((ErrorResponse) responseMessage).getState();
        } else if (responseMessage instanceof AuthorizationCodeResponse) {
            state = ((AuthorizationCodeResponse) responseMessage).getState();
        }
        if (null == state) {
            throw new ValidationFailedException( "Oauth2 response did not contain a state." );
        }

        //does the state match messages we've sent?
        AuthnProtocolRequestContext authnRequest = ProtocolContext.findContext( request.getSession(), state );
        if (authnRequest == null || !state.equals( authnRequest.getId() ))
            throw new ValidationFailedException( "Request's OAuth2 response state does not match that of any active requests." );
        logger.dbg( "OAuth2 response matches request: " + authnRequest );

        String userId = "";
        String clientId = authnRequest.getIssuer();
        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        if (!(responseMessage instanceof ErrorResponse)) {
            try {
                //ok, got a response, and it's valid, now for the rest of the OAuth flow
                //fetch access + refresh token using the code (include credentials)
                AccessTokenResponse tokenResponse = getAccessToken( ((AuthorizationCodeResponse) responseMessage).getCode(), authnRequest );
                String accessToken = tokenResponse.getAccessToken();

                //validate access token (we need to get the linkid userid (and verify application name), this is provided here)
                OAuth2TokenValidationHandler handler = OAuth2TokenValidationHandler.getInstance( authnContext.getApplicationName() );
                handler.setSslCertificate( authnContext.getOauth2().getSslCertificate() );
                handler.validateAccessToken( accessToken, null, clientId, true );
                userId = handler.getUserId( accessToken );

                //call attribute service with our token
                attributes = getAttributes( accessToken );
                success = true;
            }
            catch (Exception e) {
                throw new InternalInconsistencyException( e );
            }
        } else {
            success = userRefusedAccess( (ErrorResponse) responseMessage );
        }
        // note: oauth does not support returning authenticated devices
        AuthenticationContext authnContext = responseToContext.apply(
                new AuthnProtocolResponseContext( authnRequest, null, userId, authnRequest.getIssuer(), null, attributes, true, null, null ) );
        authnRequest = new AuthnProtocolRequestContext( null, authnContext.getApplicationName(), this,
                null != authnContext.getTarget()? authnContext.getTarget(): authnRequest.getTarget(), authnRequest.isMobileAuthentication(),
                authnRequest.isMobileAuthenticationMinimal(), authnRequest.isMobileForceRegistration() );

        return new AuthnProtocolResponseContext( authnRequest, state, userId, clientId, new LinkedList<String>(), attributes, success, null, null );
    }

    protected boolean userRefusedAccess(ErrorResponse errorResponse)
            throws ValidationFailedException {

        if (OAuth2Message.ErrorType.ACCESS_DENIED == errorResponse.getErrorType())
            return true;
        else {
            logger.err( "Received error response for OAuth authorization request: " + errorResponse.toString() );
            throw new ValidationFailedException( "Error response returned for OAuth authorization request: " + errorResponse.getErrorDescription() );
        }
    }

    /**
     * Request the oauth access token from the linkID server
     */
    protected AccessTokenResponse getAccessToken(String code, AuthnProtocolRequestContext authnRequest)
            throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, ValidationFailedException {

        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        tokenRequest.setCode( code );
        tokenRequest.setGrantType( OAuth2Message.GrantType.AUTHORIZATION_CODE );
        tokenRequest.setRedirectUri( getLandingUrl() );

        String endpoint = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().tokenPath() );

        ResponseMessage tokenResponse = MessageUtils.sendRequestMessage( endpoint, tokenRequest, authnContext.getOauth2().getSslCertificate(),
                authnRequest.getIssuer(), config().proto().oauth2().clientSecret() );

        if (tokenResponse instanceof ErrorResponse) {
            logger.err( "Received error response for OAuth authorization request: " + tokenResponse.toString() );
            throw new ValidationFailedException(
                    "Error response returned for OAuth authorization request: " + ((ErrorResponse) tokenResponse).getErrorDescription() );
        }

        return (AccessTokenResponse) tokenResponse;
    }

    /**
     * get target url. For redirection after landing
     */
    protected String getTargetUrl() {

        String targetURL = authnContext.getTarget();
        if (targetURL == null || !URI.create( targetURL ).isAbsolute())
            targetURL = ConfigUtils.getApplicationURLForPath( targetURL );
        logger.dbg( "target url: %s", targetURL );
        return targetURL;
    }

    /**
     * get landing url (=url where linkid response needs to go, ie. in oauth term the redirection URI)
     */
    protected String getLandingUrl() {

        String targetURL = getTargetUrl();
        String landingURL = null;
        if (config().web().landingPath() != null)
            landingURL = ConfigUtils.getApplicationConfidentialURLFromPath( config().web().landingPath() );
        logger.dbg( "landing url: %s", landingURL );

        if (landingURL == null) {
            // If no landing URL is configured, land on target.
            landingURL = targetURL;
        }
        return landingURL;
    }

    /**
     * Request info on the accesstoken at the linkid server. Returns userid, intended application, and so on
     */
    protected ValidationResponse getTokenInfo(String accessToken, AuthnProtocolRequestContext authnRequest)
            throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, ValidationFailedException {

        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setAccessToken( accessToken );
        String endpoint = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().validationPath() );
        ResponseMessage validationResponse = MessageUtils.sendRequestMessage( endpoint, validationRequest, authnContext.getOauth2().getSslCertificate(),
                authnRequest.getIssuer(), config().proto().oauth2().clientSecret() );

        if (validationResponse instanceof ErrorResponse) {
            logger.err( "Received error response for OAuth authorization request: " + validationResponse.toString() );
            throw new ValidationFailedException(
                    "Error response returned for OAuth authorization request: " + ((ErrorResponse) validationResponse).getErrorDescription() );
        }
        if (!authnRequest.getIssuer().equals( ((ValidationResponse) validationResponse).getAudience() )) {
            throw new ValidationFailedException( "OAuth token is not for this application" );
        }
        Long expiresIn = ((ValidationResponse) validationResponse).getExpiresIn();
        if (null == expiresIn || expiresIn <= 0) {
            throw new ValidationFailedException( "OAuth access token has expired" );
        }

        return (ValidationResponse) validationResponse;
    }

    /**
     * Get attributes from the OAuth attribte service. This service is not part of the oauth spec, so
     * this method is not present in {@code MessageUtils} from the OAuth lib
     */
    protected Map<String, List<AttributeSDK<?>>> getAttributes(String accessToken)
            throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, ValidationFailedException {

        URL endpointURL = new URL( ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().attributesPath() ) );
        HttpsURLConnection connection = (HttpsURLConnection) endpointURL.openConnection();
        SSLContext sslContext = SSLContext.getInstance( "SSL" );
        TrustManager trustManager = new MessageUtils.OAuthCustomTrustManager( authnContext.getOauth2().getSslCertificate() );
        TrustManager[] trustManagers = { trustManager };
        sslContext.init( null, trustManagers, null );
        connection.setSSLSocketFactory( sslContext.getSocketFactory() );
        if (null == authnContext.getOauth2().getSslCertificate()) {
            connection.setHostnameVerifier( new HostnameVerifier() {
                @Override
                public boolean verify(final String s, final SSLSession sslSession) {

                    logger.wrn( "Warning: URL Host: " + s + " vs. " + sslSession.getPeerHost() );
                    return true;
                }
            } );
        }
        connection.setDoOutput( true );
        connection.setDoInput( true );
        connection.setAllowUserInteraction( false );
        connection.setUseCaches( false );
        connection.setInstanceFollowRedirects( false );
        connection.setRequestMethod( MessageUtils.HttpMethod.GET.toString() );
        connection.setRequestProperty( "Authorization", "Bearer " + accessToken );
        PrintWriter contentWriter = new PrintWriter( connection.getOutputStream() );
        contentWriter.close();

        InputStreamReader reader = new InputStreamReader( connection.getInputStream() );

        Type type = new TypeToken<Map<String, List<JSONAttribute>>>() {
        }.getType();
        Map<String, List<JSONAttribute>> jsonAttributes = gson.fromJson( reader, type );
        logger.dbg( "attributes: " + jsonAttributes );

        Map<String, List<AttributeSDK<?>>> attributes = convertToSDK( jsonAttributes );

        reader.close();
        return attributes;
    }

    private Map<String, List<AttributeSDK<?>>> convertToSDK(final Map<String, List<JSONAttribute>> jsonAttributes) {

        if (jsonAttributes == null)
            return null;
        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        for (String name : jsonAttributes.keySet()) {
            List<AttributeSDK<?>> values = new ArrayList<AttributeSDK<?>>( jsonAttributes.get( name ).size() );
            for (JSONAttribute jsonAttribute : jsonAttributes.get( name )) {
                values.add( convertToSDK( jsonAttribute ) );
            }
            attributes.put( name, values );
        }
        return attributes;
    }

    private static AttributeSDK<Serializable> convertToSDK(JSONAttribute jsonAttribute) {

        if (jsonAttribute == null)
            return null;
        AttributeSDK<Serializable> attributeSDK = new AttributeSDK<Serializable>();
        attributeSDK.setId( jsonAttribute.getId() );
        attributeSDK.setName( jsonAttribute.getName() );
        if (jsonAttribute.getType() != DataType.COMPOUNDED) {
            attributeSDK.setValue( convertValue( jsonAttribute.getValue(), jsonAttribute.getType() ) );
        } else {
            List<AttributeSDK<Serializable>> members = new ArrayList<AttributeSDK<Serializable>>( jsonAttribute.getMembers().size() );
            for (JSONAttribute jsonMember : jsonAttribute.getMembers()) {
                members.add( convertToSDK( jsonMember ) );
            }
            Compound compound = new Compound( members );
            attributeSDK.setValue( compound );
        }
        return attributeSDK;
    }

    private static Serializable convertValue(String stringValue, final DataType attributeType) {

        if (null == stringValue)
            return null;

        switch (attributeType) {
            case STRING:
                return stringValue;
            case BOOLEAN:
                return Boolean.valueOf( stringValue );
            case INTEGER:
                return Integer.valueOf( stringValue );
            case DOUBLE:
                return Double.valueOf( stringValue );
            case DATE:
                return new DateTime( stringValue ).toDate();
            case COMPOUNDED:
                throw new InternalInconsistencyException( String.format( "Unexpected data type: %s", attributeType ) );
        }

        throw new InternalInconsistencyException( String.format( "Unexpected data type: %s", attributeType ) );
    }

    @Override
    public AuthnProtocolResponseContext findAndValidateAuthnAssertion(final HttpServletRequest request,
                                                                      final Function<AuthnProtocolResponseContext, AuthenticationContext> responseToContext)
            throws ValidationFailedException {

        logger.dbg( "OAuth2 implementation does not support detached authentication yet" );
        return null;
    }

    @Override
    public LogoutProtocolRequestContext sendLogoutRequest(final HttpServletResponse response, final String userId, final LogoutContext context)
            throws IOException {

        throw new UnsupportedOperationException( "OAuth2 implementation does not support single logout yet" );
    }

    @Override
    public LogoutProtocolResponseContext findAndValidateLogoutResponse(final HttpServletRequest request)
            throws ValidationFailedException {

        throw new UnsupportedOperationException( "OAuth2 implementation does not support single logout yet" );
    }

    @Override
    public LogoutProtocolRequestContext findAndValidateLogoutRequest(final HttpServletRequest request,
                                                                     final Function<LogoutProtocolRequestContext, LogoutContext> requestToContext)
            throws ValidationFailedException {

        return null;
    }

    @Override
    public LogoutProtocolResponseContext sendLogoutResponse(final HttpServletResponse response, final LogoutProtocolRequestContext logoutRequestContext,
                                                            final boolean partialLogout)
            throws IOException {

        throw new UnsupportedOperationException( "OAuth2 implementation does not support single logout yet" );
    }
}
