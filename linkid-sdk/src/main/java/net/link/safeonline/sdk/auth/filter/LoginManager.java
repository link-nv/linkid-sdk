/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.payment.PaymentResponseDO;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.util.common.CertificateChain;


/**
 * Login manager for servlet container based web applications. The login status is saved on the HTTP session.
 * <p/>
 * <p> Notice that we explicitly disconnected the login manager from the authentication protocol manager. Both store their data into the
 * HTTP session. </p>
 *
 * @author fcorneli
 */
public abstract class LoginManager {

    public static final String USERID_SESSION_ATTRIBUTE                = LoginManager.class.getName() + ".userId";
    public static final String AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE = LoginManager.class.getName() + ".authenticatedDevices";
    public static final String ATTRIBUTES_SESSION_ATTRIBUTE            = LoginManager.class.getName() + ".attributes";
    public static final String CERTIFCATE_CHAIN_SESSION_ATTRIBUTE      = LoginManager.class.getName() + ".certificateChain";
    public static final String PROTOCOL_SESSION_ATTRIBUTE              = LoginManager.class.getName() + ".protocol";
    // e.g. SAML2 assertion sent directly to SP after authentication done from a client app
    public static final String PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE   = LoginManager.class.getName() + ".protocolNoRequest";
    public static final String PAYMENT_RESPONSE_SESSION_ATTRIBUTE      = LoginManager.class.getName() + ".paymentResponse";

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not.
     *
     * @param httpSession The session from which to look up the credentials.
     *
     * @return true if linkID user credentials are available.
     */
    public static boolean isAuthenticated(HttpSession httpSession) {

        return findUserId( httpSession ) != null;
    }

    /**
     * Gives back the SafeOnline authenticated userId, or {@code null} if the user was not yet authenticated.
     *
     * @param httpSession The session from which to look up the credentials.
     *
     * @return The linkID user identifier (possibly mapped to the application or subscription) of the user that has last authenticated
     *         himself on the given session.
     */
    public static String findUserId(HttpSession httpSession) {

        return (String) httpSession.getAttribute( USERID_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the SafeOnline authenticated device, or {@code null} if the user was not yet authenticated.
     *
     * @param httpSession The session from which to look up the credentials.
     *
     * @return The linkID devices with which the user that last authenticated himself on the given session had been authenticated.
     */
    @SuppressWarnings("unchecked")
    public static List<String> findAuthenticatedDevices(HttpSession httpSession) {

        return (List<String>) httpSession.getAttribute( AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the LinkID attributes, or {@code null} if the user was not yet authenticated or no attributes were included.
     *
     * @param httpSession The session from which to look up the credentials.
     *
     * @return The linkID attributes that were sent with the last authentication response on the given session.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<AttributeSDK<Serializable>>> findAttributes(HttpSession httpSession) {

        return (Map<String, List<AttributeSDK<Serializable>>>) httpSession.getAttribute( ATTRIBUTES_SESSION_ATTRIBUTE );
    }

    /**
     * Remove the attributes map from the session.
     *
     * @param httpSession the HTTP session.
     */
    public static void cleanupAttributes(HttpSession httpSession) {

        httpSession.removeAttribute( ATTRIBUTES_SESSION_ATTRIBUTE );
    }

    @SuppressWarnings("unchecked")
    public static CertificateChain findCertificateChain(HttpSession httpSession) {

        return (CertificateChain) httpSession.getAttribute( CERTIFCATE_CHAIN_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the optional payment response case am authentication with payment context in it was started.
     */
    public static PaymentResponseDO findPaymentResponse(final HttpSession httpSession) {

        return (PaymentResponseDO) httpSession.getAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE );
    }

    /**
     * Cleanup linkId session attributes.
     *
     * @param httpSession The session from which the credentials will be removed.
     */
    public static void cleanup(HttpSession httpSession) {

        httpSession.removeAttribute( USERID_SESSION_ATTRIBUTE );
        cleanupAttributes( httpSession );
        httpSession.removeAttribute( AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE );
        httpSession.removeAttribute( CERTIFCATE_CHAIN_SESSION_ATTRIBUTE );
        httpSession.removeAttribute( PROTOCOL_SESSION_ATTRIBUTE );
        httpSession.removeAttribute( PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE );
        httpSession.removeAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE );
    }

    /**
     * This method is invoked by the SDK after handling an authentication response to make the authentication data available to the
     * application.
     *
     * @param httpSession     The session on which the credentials will be made available.
     * @param responseContext response context containing e.g.
     *                        the userId of the SafeOnline authenticated principal.
     *                        the attributes from the identity of the authenticated application.
     *                        the devices the SafeOnline authenticated principal used to authenticate with.
     *                        the certificate chain optionally present if response was signed and contained it embedded
     */
    public static void set(final HttpSession httpSession, final AuthnProtocolResponseContext responseContext) {

        httpSession.setAttribute( USERID_SESSION_ATTRIBUTE, responseContext.getUserId() );
        httpSession.setAttribute( ATTRIBUTES_SESSION_ATTRIBUTE, responseContext.getAttributes() );
        httpSession.setAttribute( AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE, responseContext.getAuthenticatedDevices() );
        httpSession.setAttribute( CERTIFCATE_CHAIN_SESSION_ATTRIBUTE, responseContext.getCertificateChain() );
        httpSession.setAttribute( PROTOCOL_SESSION_ATTRIBUTE, responseContext.getRequest().getProtocolHandler().getProtocol() );
        httpSession.setAttribute( PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE, null == responseContext.getRequest().getId() );
        httpSession.setAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE, responseContext.getPaymentResponse() );
    }
}
