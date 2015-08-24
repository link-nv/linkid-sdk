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
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthServiceClient;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnSession;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;
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

    /**
     * Start a linkID authentication/payment session using the WS binding
     *
     * @param wsUsernameConfiguration the WS-Security username config
     * @param authenticationContext   authentication context
     * @param userAgent               optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthnSession} object
     *
     * @throws LinkIDAuthnException something went wrong, check the error code what
     */
    public static LinkIDAuthnSession startAuthentication(final LinkIDWSUsernameConfiguration wsUsernameConfiguration,
                                                         final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthnException {

        return startAuthentication( getAuthServiceClient( wsUsernameConfiguration ), authenticationContext, userAgent );
    }

    /**
     * Start a linkID authentication/payment session using the WS binding.
     *
     * @param authenticationContext authentication context
     * @param userAgent             optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthnSession} object
     *
     * @throws LinkIDAuthnException something went wrong, check the error code what
     */
    public static LinkIDAuthnSession startAuthentication(final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthnException {

        return startAuthentication( LinkIDServiceFactory.getAuthService(), authenticationContext, userAgent );
    }

    /**
     * Start a linkID authentication/payment session using the WS binding
     *
     * @param linkIDAuthServiceClient the linkID authentication web service client
     * @param authenticationContext   authentication context
     * @param userAgent               optional user agent which will be used for constructing the QR code URL
     *
     * @return the {@link LinkIDAuthnSession} object
     *
     * @throws LinkIDAuthnException something went wrong, check the error code what
     */
    public static LinkIDAuthnSession startAuthentication(final LinkIDAuthServiceClient<AuthnRequest, Response> linkIDAuthServiceClient,
                                                         final LinkIDAuthenticationContext authenticationContext, final String userAgent)
            throws LinkIDAuthnException {

        Map<String, String> deviceContextMap = LinkIDDeviceContextUtils.generate( authenticationContext.getAuthenticationMessage(),
                authenticationContext.getFinishedMessage(), authenticationContext.getIdentityProfiles(), authenticationContext.getSessionExpiryOverride(),
                authenticationContext.getTheme(), authenticationContext.getMobileLandingSuccess(), authenticationContext.getMobileLandingError(),
                authenticationContext.getMobileLandingCancel() );

        AuthnRequest samlRequest = LinkIDAuthnRequestFactory.createAuthnRequest( authenticationContext.getApplicationName(), null,
                authenticationContext.getApplicationFriendlyName(), "http://foo.bar", null, authenticationContext.isForceAuthentication(), deviceContextMap,
                authenticationContext.getSubjectAttributes(), authenticationContext.getPaymentContext(), authenticationContext.getCallback() );

        return linkIDAuthServiceClient.start( samlRequest, null != authenticationContext.getLanguage()? authenticationContext.getLanguage().getLanguage(): null,
                userAgent );
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

    public static LinkIDPollResponse<Response> pollAuthentication(final LinkIDAuthServiceClient<AuthnRequest, Response> linkIDAuthServiceClient,
                                                                  final String sessionId, final Locale locale)
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
