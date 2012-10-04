package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import java.net.URI;
import java.net.URISyntaxException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;


/**
 * Validates the redirection URI.
 * <p/>
 * Date: 22/03/12
 * Time: 17:40
 *
 * @author: sgdesmet
 */
public class RedirectionURIValidator extends AbstractValidator {

    protected boolean ignoreQuery;

    /**
     * Set ignoreQuery to true to allow redirectionURIs to have different query strings
     */
    public RedirectionURIValidator(final boolean ignoreQuery) {

        this.ignoreQuery = ignoreQuery;
    }

    public RedirectionURIValidator() {

        this( false );
    }

    public boolean isIgnoreQuery() {

        return ignoreQuery;
    }

    /**
     * Set ignoreQuery to true to allow redirectionURIs to have different query strings
     */
    public void setIgnoreQuery(final boolean ignoreQuery) {

        this.ignoreQuery = ignoreQuery;
    }

    @Override
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OAuthException {

        URI redirectURI = null;
        if (!MessageUtils.collectionEmpty( configuration.getRedirectUris() ) && !MessageUtils.stringEmpty( request.getRedirectUri() )) {

            for (String configuredURI : configuration.getRedirectUris()) {
                try {
                    if (uriEquals( configuredURI, request.getRedirectUri() )) {
                        redirectURI = new URI( request.getRedirectUri() );
                    }
                }
                catch (URISyntaxException e) {
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                            String.format( "Invalid return_uri: %s - %s", request.getRedirectUri(), e.getMessage() ) );
                }
            }
            if (redirectURI == null) {
                throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                        String.format( "return_uri in request (%s) does not match any return_uri in application configuration",
                                request.getRedirectUri() ) );
            }
        } else if (MessageUtils.collectionEmpty( configuration.getRedirectUris() )) {
            if (!configuration.isConfidential() || request.getResponseType() == OAuth2Message.ResponseType.TOKEN) {
                throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                        "Public applications and/or implicit grant flows must have a pre-registered redirection URI" );
            } else {
                try {
                    redirectURI = new URI( request.getRedirectUri() );
                }
                catch (URISyntaxException e) {
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                            String.format( "Invalid return_uri: %s - %s", request.getRedirectUri(), e.getMessage() ) );
                }
            }
        } else if (MessageUtils.stringEmpty( request.getRedirectUri() )) {
            try {
                redirectURI = new URI( configuration.getRedirectUris().get( 0 ) ); // pick the first configured
            }
            catch (URISyntaxException e) {
                throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST, "Invalid return_uri: " + e.getMessage() );
            }
        } else {
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                    "No return_uri found in request or application configuration" );
        }
        // redirect URI must be safe
        // TODO: Don't blacklist, whitelist.  if ! equals https.
        //        if (redirectURI.getScheme().equals( "http" )){
        //            throw new OauthValidationException(OAuth2Message.ErrorType.INVALID_REQUEST, "Unsafe redirection URI: " + redirectURI);
        //        }
        // redirect URI must be absolute and not contain a fragment
        if (!redirectURI.isAbsolute() || !MessageUtils.stringEmpty( redirectURI.getFragment() )) {
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST,
                    "A redirection URI must be absolute and must not contain a fragment: " + redirectURI );
        }
    }

    private final boolean uriEquals(String uri1, String uri2)
            throws URISyntaxException {

        if (ignoreQuery) {
            if (uri1.indexOf( '?' ) >= 0) {
                uri1 = uri1.substring( 0, uri1.indexOf( '?' ) );
            }
            if (uri2.indexOf( '?' ) >= 0) {
                uri2 = uri2.substring( 0, uri2.indexOf( '?' ) );
            }
        }

        return new URI( uri1 ).equals( new URI( uri2 ) );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest,
                         final ClientConfiguration clientConfiguration)
            throws OAuthException {

        if (request.getGrantType() == OAuth2Message.GrantType.AUTHORIZATION_CODE) {
            if (request.getRedirectUri() != null && !request.getRedirectUri().equals( clientAccessRequest.getValidatedRedirectionURI() ))
                throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST, "redirect_uri does not match previous value" );
        }
    }
}
