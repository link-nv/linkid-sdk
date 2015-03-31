package net.link.safeonline.sdk.auth.protocol.ws;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.callback.LinkIDCallback;
import net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentContext;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthServiceClient;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnSession;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.LinkIDWSUsernameConfiguration;
import net.link.safeonline.sdk.ws.auth.LinkIDAuthServiceClientImpl;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 02/05/14
 * Time: 11:06
 */
public abstract class LinkIDAuthWSUtils {

    public static LinkIDAuthnSession startAuthentication(final LinkIDWSUsernameConfiguration linkIDWSUsernameConfiguration,
                                                   @Nullable final String authenticationMessage, @Nullable final String finishedMessage,
                                                   @Nullable final Map<String, List<Serializable>> attributeSuggestions,
                                                   @Nullable final LinkIDPaymentContext paymentContext, @Nullable final LinkIDCallback linkIDCallback,
                                                   @Nullable final List<String> identityProfiles, final Locale locale, final String userAgent,
                                                   boolean forceRegistration)
            throws LinkIDAuthnException {

        return startAuthentication( getAuthServiceClient( linkIDWSUsernameConfiguration ), linkIDWSUsernameConfiguration.getApplicationName(),
                authenticationMessage, finishedMessage, attributeSuggestions, paymentContext, linkIDCallback, identityProfiles, locale, userAgent,
                forceRegistration );
    }

    /**
     * Start a linkID authentication over SOAP WS.
     *
     * @param applicationName       the technical application name, this is the name you agreed on with the linkID team
     * @param authenticationMessage optional authentication message, e.g. custom context to be shown on the user's mobile
     * @param finishedMessage       optional authentication finished message, e.g. custom context to be shown on the user's mobile
     * @param attributeSuggestions  optional map of attribute suggestions
     * @param paymentContext        optional payment context
     * @param linkIDCallback            optional callback config
     * @param identityProfiles      optional list of identity profile names to use for this authentication, if null or empty, the default configured @ linkID
     *                              will be used
     * @param locale                Locale of the authentication
     * @param userAgent             optional user agent which will be used for constructing the QR code URL
     * @param forceRegistration     force registration or not
     *
     * @return the {@link LinkIDAuthnSession} object
     */
    public static LinkIDAuthnSession startAuthentication(final String applicationName, @Nullable final String authenticationMessage,
                                                   @Nullable final String finishedMessage, @Nullable final Map<String, List<Serializable>> attributeSuggestions,
                                                   @Nullable final LinkIDPaymentContext paymentContext, @Nullable final LinkIDCallback linkIDCallback,
                                                   @Nullable final List<String> identityProfiles, final Locale locale, final String userAgent,
                                                   boolean forceRegistration)
            throws LinkIDAuthnException {

        return startAuthentication( LinkIDServiceFactory.getAuthService(), applicationName, authenticationMessage, finishedMessage, attributeSuggestions,
                paymentContext, linkIDCallback, identityProfiles, locale, userAgent, forceRegistration );
    }

    public static LinkIDAuthnSession startAuthentication(LinkIDAuthServiceClient<AuthnRequest, Response> linkIDAuthServiceClient, final String applicationName,
                                                   @Nullable final String authenticationMessage, @Nullable final String finishedMessage,
                                                   @Nullable final Map<String, List<Serializable>> attributeSuggestions,
                                                   @Nullable final LinkIDPaymentContext paymentContext, @Nullable final LinkIDCallback linkIDCallback,
                                                   @Nullable final List<String> identityProfiles, final Locale locale, final String userAgent,
                                                   boolean forceRegistration)
            throws LinkIDAuthnException {

        Map<String, String> deviceContextMap = LinkIDDeviceContextUtils.generate( authenticationMessage, finishedMessage, identityProfiles );

        AuthnRequest samlRequest = LinkIDAuthnRequestFactory.createAuthnRequest( applicationName, null, null, "http://foo.bar", null, false, deviceContextMap,
                attributeSuggestions, paymentContext, linkIDCallback );

        return linkIDAuthServiceClient.start( samlRequest, locale.getLanguage(), userAgent, forceRegistration );
    }

    /**
     * Poll the linkID authentication state
     *
     * @param sessionId the linkID session's ID
     *
     * @return the state of the linkID session
     */
    public static LinkIDPollResponse<Response> pollAuthentication(final String sessionId, final Locale locale)
            throws LinkIDPollException {

        return pollAuthentication( LinkIDServiceFactory.getAuthService(), sessionId, locale );
    }

    public static LinkIDPollResponse<Response> pollAuthentication(final LinkIDWSUsernameConfiguration linkIDWSUsernameConfiguration, final String sessionId,
                                                            final Locale locale)
            throws LinkIDPollException {

        return pollAuthentication( getAuthServiceClient( linkIDWSUsernameConfiguration ), sessionId, locale );
    }

    public static LinkIDPollResponse<Response> pollAuthentication(final LinkIDAuthServiceClient<AuthnRequest, Response> linkIDAuthServiceClient, final String sessionId,
                                                            final Locale locale)
            throws LinkIDPollException {

        return linkIDAuthServiceClient.poll( sessionId, locale.getLanguage() );
    }

    public static LinkIDAuthServiceClient<AuthnRequest, Response> getAuthServiceClient(final LinkIDWSUsernameConfiguration linkIDWSUsernameConfiguration) {

        return new LinkIDAuthServiceClientImpl( LinkIDServiceFactory.getWsUsernameBase( linkIDWSUsernameConfiguration.getLinkIDBase() ),
                linkIDWSUsernameConfiguration.getSSLCertificates(), new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return linkIDWSUsernameConfiguration.getUsername();
            }

            @Override
            public String getPassword() {

                return linkIDWSUsernameConfiguration.getPassword();
            }

            @Nullable
            @Override
            public String handle(final String username) {

                return null;
            }

            @Override
            public boolean isInboundHeaderOptional() {

                return true;
            }
        } );
    }

    /**
     * Parses the SAML v2.0 response
     *
     * @param response the SAML v2.0 response
     */
    public static LinkIDAuthnResponse parse(final Response response) {

        String userId = null;
        Map<String, List<LinkIDAttribute<Serializable>>> attributes = Maps.newHashMap();
        if (!response.getAssertions().isEmpty()) {
            Assertion assertion = response.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
        }

        LinkIDPaymentResponse paymentResponse = LinkIDSaml2Utils.findPaymentResponse( response );
        LinkIDExternalCodeResponse externalCodeResponse = LinkIDSaml2Utils.findExternalCodeResponse( response );

        return new LinkIDAuthnResponse( userId, attributes, paymentResponse, externalCodeResponse );
    }
}
