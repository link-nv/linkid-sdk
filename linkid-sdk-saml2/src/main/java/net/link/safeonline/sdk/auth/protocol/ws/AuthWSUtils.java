package net.link.safeonline.sdk.auth.protocol.ws;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.auth.AuthnResponseDO;
import net.link.safeonline.sdk.api.auth.device.DeviceContextConstants;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.api.payment.PaymentResponseDO;
import net.link.safeonline.sdk.api.ws.auth.AuthServiceClient;
import net.link.safeonline.sdk.api.ws.auth.AuthnException;
import net.link.safeonline.sdk.api.ws.auth.AuthnSession;
import net.link.safeonline.sdk.api.ws.auth.PollException;
import net.link.safeonline.sdk.api.ws.auth.PollResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.AuthnRequestFactory;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;


/**
 * Created by wvdhaute
 * Date: 02/05/14
 * Time: 11:06
 */
public abstract class AuthWSUtils {

    /**
     * Start a linkID authentication over SOAP WS.
     *
     * @param applicationName       the technical application name, this is the name you agreed on with the linkID team
     * @param authenticationMessage optional authentication message, e.g. custom context to be shown on the user's mobile
     * @param finishesMessage       optional authentication finished message, e.g. custom context to be shown on the user's mobile
     * @param attributeSuggestions  optional map of attribute suggestions
     * @param paymentContext        optional payment context
     * @param locale                Locale of the authentication
     * @param userAgent             optional user agent which will be used for constructing the QR code URL
     * @param forceRegistration     force registration or not
     *
     * @return the {@link AuthnSession} object
     */
    public static AuthnSession startAuthentication(final String applicationName, @Nullable final String authenticationMessage,
                                                   @Nullable final String finishesMessage, @Nullable final Map<String, List<Serializable>> attributeSuggestions,
                                                   @Nullable final PaymentContextDO paymentContext, final Locale locale, final String userAgent,
                                                   boolean forceRegistration)
            throws AuthnException {

        AuthServiceClient<AuthnRequest, Response> authServiceClient = LinkIDServiceFactory.getAuthService();

        Map<String, String> deviceContextMap = Maps.newHashMap();
        if (null != authenticationMessage) {
            deviceContextMap.put( DeviceContextConstants.AUTHENTICATION_MESSAGE, authenticationMessage );
        }
        if (null != finishesMessage) {
            deviceContextMap.put( DeviceContextConstants.FINISHED_MESSAGE, finishesMessage );
        }

        AuthnRequest samlRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, "http://foo.bar", null, false, deviceContextMap,
                attributeSuggestions, paymentContext );

        return authServiceClient.start( samlRequest, locale.getLanguage(), userAgent, forceRegistration );
    }

    /**
     * Poll the linkID authentication state
     *
     * @param sessionId the linkID session's ID
     *
     * @return the state of the linkID session
     */
    public static PollResponse<Response> pollAuthentication(final String sessionId, final Locale locale)
            throws PollException {

        AuthServiceClient<AuthnRequest, Response> authServiceClient = LinkIDServiceFactory.getAuthService();

        return authServiceClient.poll( sessionId, locale.getLanguage() );
    }

    /**
     * Parses the SAML v2.0 response
     *
     * @param response the SAML v2.0 response
     */
    public static AuthnResponseDO parse(final Response response) {

        String userId = null;
        Map<String, List<AttributeSDK<Serializable>>> attributes = Maps.newHashMap();
        if (!response.getAssertions().isEmpty()) {
            Assertion assertion = response.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( LinkIDSaml2Utils.getAttributeValues( assertion ) );
        }

        PaymentResponseDO paymentResponse = LinkIDSaml2Utils.findPaymentResponse( response );

        return new AuthnResponseDO( userId, attributes, paymentResponse );
    }
}
