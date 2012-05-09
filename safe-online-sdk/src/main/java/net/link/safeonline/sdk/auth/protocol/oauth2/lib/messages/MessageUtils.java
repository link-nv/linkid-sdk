package net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages;

import java.io.*;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthInvalidMessageException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;


/**
 * TODO description
 * <p/>
 * Date: 22/03/12
 * Time: 16:34
 *
 * @author: sgdesmet
 */
public class MessageUtils {

    private static final Log LOG = LogFactory.getLog( MessageUtils.class );

    private static String authenticationRequiredHeader = "Basic realm=\"oauth\"";

    public static String getAuthenticationRequiredHeader() {

        return authenticationRequiredHeader;
    }

    public static void setAuthenticationRequiredHeader(final String authenticationRequiredHeader) {

        MessageUtils.authenticationRequiredHeader = authenticationRequiredHeader;
    }

    /**
     * Gets an authorization request from servlet request.
     * Throws an exception if the request is invalid.
     * @param request
     * @return
     * @throws OauthInvalidMessageException
     */
    public static AuthorizationRequest getAuthorizationRequest(final HttpServletRequest request)
            throws OauthInvalidMessageException {

        // do some validation of the http connection
        validateHttpRequest( request );
        // just to make sure the client secret is not present here, or it would be exposed
        if (!stringEmpty( request.getParameter( OAuth2Message.CLIENT_SECRET ) )){
            throw new OauthInvalidMessageException("Client secret is exposed, never include it in an authorization request");
        }

        AuthorizationRequest authRequest = new AuthorizationRequest();
        authRequest.setResponseType( OAuth2Message.ResponseType
                                                  .fromString( request.getParameter( OAuth2Message.RESPONSE_TYPE ) ) );
        authRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
        authRequest.setState( request.getParameter( OAuth2Message.STATE ) );
        authRequest.setRedirectUri( request.getParameter( OAuth2Message.REDIRECT_URI ) );
        String scope = request.getParameter( OAuth2Message.SCOPE );
        if ( !stringEmpty( scope ) ){
            authRequest.setScope( Arrays.asList( scope.split( " " ) ) );
        } else {
            authRequest.setScope( Collections.<String>emptyList() );
        }
        return authRequest;
    }

    /**
     * Gets a token request from the http request. If present, extracts either http basic auth client credentials  or form-based auth
     * client credentials, and stores them in the AccessTokenRequest return message. Note that client credentials in the query string
     * are not allowed.
     *
     * @param request
     * @return
     * @throws OauthInvalidMessageException
     */
    public static AccessTokenRequest getTokenRequest(final HttpServletRequest request)
            throws OauthInvalidMessageException{

        // do some validation of the http connection
        validateHttpRequest( request );

        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
        accessTokenRequest.setGrantType( OAuth2Message.GrantType.fromString( request.getParameter( OAuth2Message.GRANT_TYPE ) ) );

        // create message
        accessTokenRequest.setCode( request.getParameter( OAuth2Message.CODE ) );
        accessTokenRequest.setRedirectUri( request.getParameter( OAuth2Message.REDIRECT_URI ) );

        accessTokenRequest.setRefreshToken( request.getParameter( OAuth2Message.REFRESH_TOKEN ) );

        accessTokenRequest.setUsername( request.getParameter( OAuth2Message.USERNAME ) );
        accessTokenRequest.setPassword( request.getParameter( OAuth2Message.PASSWORD ) );

        String scope = request.getParameter( OAuth2Message.SCOPE );
        if (!stringEmpty( scope )){
            accessTokenRequest.setScope( Arrays.asList( scope.split( " " ) ) );
        } else {
            accessTokenRequest.setScope( Collections.<String>emptyList() );
        }

        // make sure the client secret is not exposed, as per OAuth2 spec
        if (!stringEmpty( request.getQueryString() ) && request.getQueryString().contains( OAuth2Message.CLIENT_SECRET ))
            throw new OauthInvalidMessageException( "Client secret is exposed in the query string, this is not allowed" );

        //get credentials from either authorization header or x-www-form-encoded
        String authHeader = request.getHeader( "Authorization" );
        if (!stringEmpty( authHeader )){
            StringTokenizer st = new StringTokenizer( authHeader.trim() );
            if (st.hasMoreTokens()){
                String type = st.nextToken();
                if ("Basic".equalsIgnoreCase( type )){
                    if (st.hasMoreTokens()){
                        String credentials =  st.nextToken();
                        try {
                            credentials = new String( new BASE64Decoder().decodeBuffer( credentials ) );
                            int index =  credentials.indexOf( ":" );
                            if (index >= 0){
                                accessTokenRequest.setClientId( credentials.substring(0, index) );
                                accessTokenRequest.setClientSecret( credentials.substring(index+1) );
                            }
                        }
                        catch (IOException e) {
                            LOG.error( "While decoding basic auth credentials string", e );
                        }
                    }
                }
            }
        }
        if (accessTokenRequest.getClientId() == null || accessTokenRequest.getClientSecret() == null){
            accessTokenRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
            accessTokenRequest.setClientSecret( request.getParameter( OAuth2Message.CLIENT_SECRET ) );
        }

        return accessTokenRequest;
    }

    /**
     * Gets a token validation request from the servlet request. Supports fetching the access_token parameter encoded
     * in the request with x-www-form-encoded, and supports Bearer tokens in the authorization header.
     * @param request
     * @return
     * @throws OauthInvalidMessageException
     */
    public static ValidationRequest getValidationMessage(final HttpServletRequest request) throws OauthInvalidMessageException{

        LOG.debug( "get access token" );
        // do some validation of the http connection
        validateHttpRequest( request );
        // just to be sure
        if (!stringEmpty( request.getQueryString() ) && request.getQueryString().contains( OAuth2Message.CLIENT_SECRET ))
            throw new OauthInvalidMessageException( "Client secret is exposed in the query string, this is not allowed" );

        // get access token from parameter or authorization header
        // if client credentials are present, add them
        ValidationRequest validationRequest = new ValidationRequest();
        String accessToken = request.getParameter( OAuth2Message.ACCESS_TOKEN );
        if (null == accessToken){
            String authHeader = request.getHeader( "Authorization" );
            if (!stringEmpty( authHeader )){
                StringTokenizer st = new StringTokenizer( authHeader.trim() );
                if (st.hasMoreTokens()){
                    String type = st.nextToken();
                    if ("Bearer".equalsIgnoreCase( type )){
                        accessToken = st.hasMoreTokens() ? st.nextToken() : null;
                    } else if ("Basic".equalsIgnoreCase( type )){
                        if (st.hasMoreTokens()){
                            String credentials =  st.nextToken();
                            try {
                                credentials = new String( new BASE64Decoder().decodeBuffer( credentials ) );
                                int index =  credentials.indexOf( ":" );
                                if (index >= 0){
                                    validationRequest.setClientId( credentials.substring(0, index) );
                                    validationRequest.setClientSecret( credentials.substring(index+1) );
                                }
                            }
                            catch (IOException e) {
                                LOG.error( "While decoding basic auth credentials string", e );
                            }
                        }
                    }
                }
            }
        }

        if ( null != accessToken )
            validationRequest.setAccessToken( accessToken );
        else
            throw new OauthInvalidMessageException("Authorization required");

        // in case of form based auth
        if (validationRequest.getClientId() == null || validationRequest.getClientSecret() == null){
            validationRequest.setClientId( request.getParameter( OAuth2Message.CLIENT_ID ) );
            validationRequest.setClientSecret( request.getParameter( OAuth2Message.CLIENT_SECRET ) );
        }

        return validationRequest;
    }

    /**
     * Throws an exception if the connection is not secure, or the http method does not equal get or post
     * @param request
     * @throws OauthInvalidMessageException
     */
    protected static final void validateHttpRequest(HttpServletRequest request)
            throws OauthInvalidMessageException {

        LOG.debug( "checking http for TLS and correct methods" );
        if (!request.isSecure() || (!stringEmpty( request.getHeader( "X-Forwarded-Proto" ) ) && request.getHeader( "X-Forwarded-Proto" )
                                                                                                       .contains( HttpScheme.HTTPS )))
            throw new OauthInvalidMessageException( "TLS is mandatory" );

        if (!request.getMethod().equalsIgnoreCase( HttpMethod.GET.toString() ) && !request.getMethod().equalsIgnoreCase(
                HttpMethod.POST.toString() )){
            throw new OauthInvalidMessageException("invalid http method type: " + request.getMethod());
        }
    }

    public static void sendResponseMessage(final HttpServletResponse servletResponse, ResponseMessage responseMessage)
            throws IOException {

        if (null == responseMessage){
            return;
        }
        servletResponse.setContentType( "application/json;charset=UTF-8" );
        servletResponse.setHeader( "Cache-Control", "no-store" );
        servletResponse.setHeader( "Pragma", "no-cache" );

        if (responseMessage instanceof AuthorizationCodeResponse){
            LOG.error( "Authorization codes must be returned by redirect" );
            return;
        } else if (responseMessage instanceof AccessTokenResponse){
            servletResponse.getWriter().print( toJson( (AccessTokenResponse) responseMessage ) );
        } else if (responseMessage instanceof ErrorResponse){
            if (responseMessage instanceof CredentialsRequiredResponse){
                servletResponse.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                servletResponse.setHeader( "WWW-Authenticate", authenticationRequiredHeader );
            } else if (((ErrorResponse) responseMessage).getErrorType() == OAuth2Message.ErrorType.SERVER_ERROR){
                servletResponse.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            } else
                servletResponse.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            servletResponse.getWriter().print( toJson( (ErrorResponse)responseMessage ) );
        } else if (responseMessage instanceof ValidationResponse ){
            servletResponse.getWriter().print( toJson( (ValidationResponse) responseMessage ) );
        }
    }

    /**
     * Sends a request message to an oauth2 endpoint
     *
     * @param endpoint
     * @param requestMessage
     * @param sslCertificate ssl certificate to trust. May be null, in which case all certificates are trusted.
     */
    public static void sendRequestMessage(String endpoint, RequestMessage requestMessage, final X509Certificate sslCertificate){
        //TODO
    }

    public static void sendRedirectMessage(final String redirectUri, ResponseMessage responseMessage,
                                           final HttpServletResponse servletResponse, boolean paramsInBody)
            throws IOException {

        if(null == responseMessage){
            return;
        }

        if (paramsInBody){
            servletResponse.setHeader( "Cache-Control", "no-store" );
            servletResponse.setHeader( "Pragma", "no-cache" );
            servletResponse.setContentType( "text/html;charset=UTF-8" );
        }

        List<String> names = new ArrayList<String>(  );
        List<Object> values = new ArrayList<Object>(  );
        if (responseMessage instanceof ValidationResponse){
            LOG.error( "Access token validations cannot be returned by redirect" );
            return;
        } else if (responseMessage instanceof AuthorizationCodeResponse){

            AuthorizationCodeResponse authorizationCodeResponse = (AuthorizationCodeResponse) responseMessage;
            names.add( OAuth2Message.CODE );
            names.add( OAuth2Message.STATE );
            values.add( authorizationCodeResponse.getCode());
            values.add( authorizationCodeResponse.getState());
        } else if (responseMessage instanceof AccessTokenResponse){

            AccessTokenResponse accessTokenResponse = (AccessTokenResponse) responseMessage;

            names.add( OAuth2Message.ACCESS_TOKEN );
            values.add (accessTokenResponse.getAccessToken());
            names.add( OAuth2Message.REFRESH_TOKEN );
            values.add (accessTokenResponse.getRefreshToken());
            names.add( OAuth2Message.EXPIRES_IN );
            values.add (accessTokenResponse.getExpiresIn());
            names.add( OAuth2Message.TOKEN_TYPE );
            values.add (accessTokenResponse.getTokenType());
            names.add( OAuth2Message.SCOPE );
            values.add (stringify( accessTokenResponse.getScope() ));

        } else if (responseMessage instanceof ErrorResponse){

            ErrorResponse errorResponse = (ErrorResponse) responseMessage;

            names.add( OAuth2Message.ERROR);
            values.add( errorResponse.getErrorType());
            names.add( OAuth2Message.ERROR_DESCRIPTION);
            values.add( errorResponse.getErrorDescription());
            names.add( OAuth2Message.ERROR_URI);
            values.add( errorResponse.getErrorUri());
            names.add( OAuth2Message.STATE);
            values.add( errorResponse.getState());
            servletResponse.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        }

        if (!paramsInBody){
            servletResponse.sendRedirect( encodeInQuery( redirectUri, names, values ) );
        } else {
            servletResponse.getWriter().print( encodeInHTMLForm( redirectUri, names, values ) );
        }
    }

    public static void sendRedirectMessage(final String redirectUri, RequestMessage requestMessage,
                                           final HttpServletResponse servletResponse, boolean paramsInBody){

    }

    private static String toJson(AccessTokenResponse response){
        StringBuffer sb = new StringBuffer(  );
        sb.append( "{\n" );
        jsonAppend( sb, OAuth2Message.ACCESS_TOKEN, response.getAccessToken() );
        jsonAppend( sb, OAuth2Message.TOKEN_TYPE, response.getTokenType() );
        jsonAppend( sb, OAuth2Message.EXPIRES_IN, response.getExpiresIn() );
        jsonAppend( sb, OAuth2Message.REFRESH_TOKEN, response.getRefreshToken() );
        jsonAppend( sb, OAuth2Message.SCOPE, stringify( response.getScope() ) );
        sb.append( "}" );
        return sb.toString();
    }

    private static String toJson(ErrorResponse response){
        StringBuffer sb = new StringBuffer(  );
        sb.append( "{\n" );
        jsonAppend( sb, OAuth2Message.ERROR, response.getErrorType().toString() );
        jsonAppend( sb, OAuth2Message.ERROR_DESCRIPTION, response.getErrorDescription() );
        jsonAppend( sb, OAuth2Message.ERROR_URI, response.getErrorUri() );
        sb.append( "}" );
        return sb.toString();
    }

    private static String toJson(ValidationResponse response){
        StringBuffer sb = new StringBuffer(  );
        sb.append( "{\n" );
        jsonAppend( sb, OAuth2Message.AUDIENCE, response.getAudience() );
        jsonAppend( sb, OAuth2Message.USER_ID, response.getUserId() );
        jsonAppend( sb, OAuth2Message.EXPIRES_IN, response.getExpiresIn() );
        jsonAppend( sb, OAuth2Message.SCOPE, stringify( response.getScope() ) );
        sb.append( "}" );
        return sb.toString();
    }

    private static void jsonAppend(StringBuffer sb, String name, String value){
        if (!stringEmpty( value )){
            sb.append( "\"" + name + "\":\""+ escape( value ) + "\"\n" );
        }
    }

    private static void jsonAppend(StringBuffer sb, String name, Long value){
        if (null != value){
            sb.append( "\"" + name + "\":" + value + "\n" );
        }
    }

    /**
     * JSON escaping, see JSONObject from json-simple http://code.google.com/p/json-simple/
     * @param s
     */
    private static String escape(String s) {
        StringBuffer sb = new StringBuffer(  );
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt( i );
            switch (ch) {
                case '"':
                    sb.append( "\\\"" );
                    break;
                case '\\':
                    sb.append( "\\\\" );
                    break;
                case '\b':
                    sb.append( "\\b" );
                    break;
                case '\f':
                    sb.append( "\\f" );
                    break;
                case '\n':
                    sb.append( "\\n" );
                    break;
                case '\r':
                    sb.append( "\\r" );
                    break;
                case '\t':
                    sb.append( "\\t" );
                    break;
                case '/':
                    sb.append( "\\/" );
                    break;
                default:
                    //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString( ch );
                        sb.append( "\\u" );
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append( '0' );
                        }
                        sb.append( ss.toUpperCase() );
                    } else {
                        sb.append( ch );
                    }
            }
        }
        return sb.toString();
    }


    protected static String encodeInQuery(String redirectUri, List<String> names, List<Object> values){
        if (names.size() != names.size()){
            LOG.error( "names and values lists are not equal size" );
            return "";
        }

        StringBuffer redirectBuff = new StringBuffer( redirectUri );
        for (int i = 0; i < names.size(); i++){
            String name =  names.get( i );
            String value =  values.get( i ) != null ? values.get( i ).toString() : null;
            if (!stringEmpty( value ) && !stringEmpty( name )){
                encodeInQuery( redirectBuff, name, value );
            }
        }
        return redirectBuff.toString();
    }

    protected static void encodeInQuery(StringBuffer sb, String name, String value){
        if(value != null){
            char symbol = sb.indexOf( "?" ) > 0 ? '&' : '?';
            try {
                sb.append( symbol + URLEncoder.encode( name, "UTF-8" ) +"=" + URLEncoder.encode( value, "UTF-8" ) );
            }
            catch (UnsupportedEncodingException e) {
                LOG.error( e );
            }
        }
    }

    protected static void encodeInQuery(StringBuffer sb, String name, Number value){
        encodeInQuery( sb, name, value.toString());
    }

    protected static String stringify(List<String> scope){
        StringBuffer buff = new StringBuffer( "" );
        if ( scope != null)
            for (Iterator<String> iterator = scope.iterator(); iterator.hasNext();){
                buff.append( iterator.next() );
                if (iterator.hasNext())
                    buff.append( ' ' );
            }
        return buff.toString();
    }

    protected static String encodeInHTMLForm(String redirectUri, List<String> names, List<Object> values){

        // okay, i'll be the first to admit that this method looks positively nasty. I'll get round to improving it, promise

        if (names.size() != names.size() ){
            LOG.error( "names and values lists are not equal size" );
            return "";
        }

        StringBuffer html = new StringBuffer(  );
        html.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">" );
        html.append( "<head>\n" + "\t\t<meta http-equiv=\"pragma\" content=\"no-cache\"/>\n"
                     + "\t\t<meta http-equiv=\"cache-control\" content=\"no-cache, must-revalidate\"/>\n"
                     + "\t\t<meta http-equiv=\"expires\" content=\"-1\"/>\n" + "\t</head>" );
        html.append( "<body onload=\"document.forms[0].submit()\">\n"
                     + "<noscript>\n" + "            <p>\n"
                     + "    <strong>Note:</strong> Since your browser does not support JavaScript,\n"
                     + "    you must press the Continue button once to proceed.\n" + "            </p>\n"
                     + "</noscript>" );
        html.append( "<form action=\"" + redirectUri + "\" method=\"post\" autocomplete=\"off\" target=\"_self\">" );

        for (int i = 0; i < names.size(); i++){
            String name =  names.get( i );
            String value =  values.get( i ) != null ? values.get( i ).toString() : null;
            if (!stringEmpty( value ) && !stringEmpty( name )){
                html.append( " <input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>" );
            }
        }

        html.append( " <noscript>\n"
                     + "    <input type=\"submit\" value=\"Continue\"/>\n"
                     + " </noscript>\n"
                     + "</form>");
        html.append( "</body>" );
        html.append( "</html>" );

        return html.toString();
    }

    public static final boolean stringEmpty(String string){
        return string == null || string.length() == 0;
    }

    public static final boolean collectionEmpty(Collection collection){
        return collection == null || collection.size() == 0;
    }


    public static enum HttpMethod {
        POST, GET, DELETE, PUT, HEAD
    }


    public static final class HttpScheme {
        public static final String HTTPS = "https";
    }
}
