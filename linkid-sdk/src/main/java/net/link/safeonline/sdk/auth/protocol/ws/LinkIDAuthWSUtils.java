package net.link.safeonline.sdk.auth.protocol.ws;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollException;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthPollResponse;
import net.link.safeonline.sdk.api.ws.linkid.auth.LinkIDAuthSession;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.safeonline.sdk.ws.LinkIDWSUsernameConfiguration;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
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
public class LinkIDAuthWSUtils {

    private LinkIDAuthWSUtils() {

        throw new AssertionError();
    }

    /**
     * Start a linkID authentication/payment session using the WS binding
     *
     * @param wsUsernameConfiguration the WS-Security username config
     * @param authenticationContext   authentication context
     * @param userAgent               optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthSession} object
     *
     * @throws LinkIDAuthException something went wrong, check the error code what
     */
    public static LinkIDAuthSession startAuthentication(final LinkIDWSUsernameConfiguration wsUsernameConfiguration,
                                                        final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthException {

        return startAuthentication( getLinkIDServiceClient( wsUsernameConfiguration ), authenticationContext, userAgent );
    }

    /**
     * Start a linkID authentication/payment session using the WS binding.
     *
     * @param authenticationContext authentication context
     * @param userAgent             optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthSession} object
     *
     * @throws LinkIDAuthException something went wrong, check the error code what
     */
    public static LinkIDAuthSession startAuthentication(final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthException {

        return startAuthentication( LinkIDServiceFactory.getLinkIDService(), authenticationContext, userAgent );
    }

    /**
     * Start a linkID authentication/payment session using the WS binding
     *
     * @param linkIDServiceClient   the linkID authentication web service client
     * @param authenticationContext authentication context
     * @param userAgent             optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthSession} object
     *
     * @throws LinkIDAuthException something went wrong, check the error code what
     */
    public static LinkIDAuthSession startAuthentication(final LinkIDServiceClient<AuthnRequest, Response> linkIDServiceClient,
                                                        final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthException {

        Map<String, String> deviceContextMap = LinkIDDeviceContextUtils.generate( authenticationContext.getAuthenticationMessage(),
                authenticationContext.getFinishedMessage(), authenticationContext.getIdentityProfile(), authenticationContext.getSessionExpiryOverride(),
                authenticationContext.getTheme(), authenticationContext.getMobileLandingSuccess(), authenticationContext.getMobileLandingError(),
                authenticationContext.getMobileLandingCancel() );

        AuthnRequest samlRequest = LinkIDAuthnRequestFactory.createAuthnRequest( authenticationContext.getApplicationName(), null,
                authenticationContext.getApplicationFriendlyName(), "http://foo.bar", null, authenticationContext.isForceAuthentication(), deviceContextMap,
                authenticationContext.getSubjectAttributes(), authenticationContext.getPaymentContext(), authenticationContext.getCallback() );

        return linkIDServiceClient.authStart( samlRequest, null != authenticationContext.getLanguage()? authenticationContext.getLanguage().getLanguage(): null,
                userAgent );
    }

    /**
     * Poll the linkID authentication state
     *
     * @param sessionId the linkID session's ID
     *
     * @return the state of the linkID session
     */
    public static LinkIDAuthPollResponse<Response> pollAuthentication(final String sessionId, final Locale locale)
            throws LinkIDAuthPollException {

        return pollAuthentication( LinkIDServiceFactory.getLinkIDService(), sessionId, locale );
    }

    public static LinkIDAuthPollResponse<Response> pollAuthentication(final LinkIDWSUsernameConfiguration linkIDWSUsernameConfiguration, final String sessionId,
                                                                      final Locale locale)
            throws LinkIDAuthPollException {

        return pollAuthentication( getLinkIDServiceClient( linkIDWSUsernameConfiguration ), sessionId, locale );
    }

    public static LinkIDAuthPollResponse<Response> pollAuthentication(final LinkIDServiceClient<AuthnRequest, Response> linkIDServiceClient,
                                                                      final String sessionId, final Locale locale)
            throws LinkIDAuthPollException {

        return linkIDServiceClient.authPoll( sessionId, locale.getLanguage() );
    }

    public static LinkIDServiceClient<AuthnRequest, Response> getLinkIDServiceClient(final LinkIDWSUsernameConfiguration linkIDWSUsernameConfiguration) {

        return new LinkIDServiceClientImpl( LinkIDServiceFactory.getWsUsernameBase( linkIDWSUsernameConfiguration.getLinkIDBase() ),
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
