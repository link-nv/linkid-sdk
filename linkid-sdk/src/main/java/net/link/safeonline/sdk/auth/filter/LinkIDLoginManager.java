/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolResponseContext;
import net.link.safeonline.sdk.configuration.LinkIDProtocol;
import net.link.util.common.CertificateChain;


/**
 * Login manager for servlet container based web applications. The login status is saved on the HTTP session.
 * <p/>
 * <p> Notice that we explicitly disconnected the login manager from the authentication protocol manager. Both store their data into the
 * HTTP session. </p>
 *
 * @author fcorneli
 */
public abstract class LinkIDLoginManager {

    public static final String USERID_SESSION_ATTRIBUTE                 = LinkIDLoginManager.class.getName() + ".userId";
    public static final String ATTRIBUTES_SESSION_ATTRIBUTE             = LinkIDLoginManager.class.getName() + ".attributes";
    public static final String CERTIFICATE_CHAIN_SESSION_ATTRIBUTE      = LinkIDLoginManager.class.getName() + ".certificateChain";
    public static final String PROTOCOL_SESSION_ATTRIBUTE               = LinkIDLoginManager.class.getName() + ".protocol";
    // e.g. SAML2 assertion sent directly to SP after authentication done from a client app
    public static final String PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE    = LinkIDLoginManager.class.getName() + ".protocolNoRequest";
    public static final String PAYMENT_RESPONSE_SESSION_ATTRIBUTE       = LinkIDLoginManager.class.getName() + ".paymentResponse";
    public static final String EXTERNAL_CODE_RESPONSE_SESSION_ATTRIBUTE = LinkIDLoginManager.class.getName() + ".externalCodeResponse";

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
     * himself on the given session.
     */
    public static String findUserId(HttpSession httpSession) {

        return (String) httpSession.getAttribute( USERID_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the LinkID attributes, or {@code null} if the user was not yet authenticated or no attributes were included.
     *
     * @param httpSession The session from which to look up the credentials.
     *
     * @return The linkID attributes that were sent with the last authentication response on the given session.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<LinkIDAttribute<Serializable>>> findAttributes(HttpSession httpSession) {

        return (Map<String, List<LinkIDAttribute<Serializable>>>) httpSession.getAttribute( ATTRIBUTES_SESSION_ATTRIBUTE );
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

        return (CertificateChain) httpSession.getAttribute( CERTIFICATE_CHAIN_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the optional payment response case am authentication with payment context in it was started.
     */
    public static LinkIDPaymentResponse findPaymentResponse(final HttpSession httpSession) {

        return (LinkIDPaymentResponse) httpSession.getAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE );
    }

    /**
     * Gives back the optional external code response.
     */
    public static LinkIDExternalCodeResponse findExternalCodeResponse(final HttpSession httpSession) {

        return (LinkIDExternalCodeResponse) httpSession.getAttribute( EXTERNAL_CODE_RESPONSE_SESSION_ATTRIBUTE );
    }

    /**
     * Cleanup linkId session attributes.
     *
     * @param httpSession The session from which the credentials will be removed.
     */
    public static void cleanup(HttpSession httpSession) {

        httpSession.removeAttribute( USERID_SESSION_ATTRIBUTE );
        cleanupAttributes( httpSession );
        httpSession.removeAttribute( CERTIFICATE_CHAIN_SESSION_ATTRIBUTE );
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
    public static void set(final HttpSession httpSession, final LinkIDAuthnProtocolResponseContext responseContext) {

        httpSession.setAttribute( USERID_SESSION_ATTRIBUTE, responseContext.getUserId() );
        httpSession.setAttribute( ATTRIBUTES_SESSION_ATTRIBUTE, responseContext.getAttributes() );
        httpSession.setAttribute( CERTIFICATE_CHAIN_SESSION_ATTRIBUTE, responseContext.getCertificateChain() );
        httpSession.setAttribute( PROTOCOL_SESSION_ATTRIBUTE, responseContext.getRequest().getLinkIDProtocolHandler().getProtocol() );
        httpSession.setAttribute( PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE, null == responseContext.getRequest().getId() );
        httpSession.setAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE, responseContext.getPaymentResponse() );
        httpSession.setAttribute( EXTERNAL_CODE_RESPONSE_SESSION_ATTRIBUTE, responseContext.getExternalCodeResponse() );
    }

    public static void set(final HttpSession httpSession, final LinkIDAuthnResponse linkIDAuthnResponse) {

        httpSession.setAttribute( USERID_SESSION_ATTRIBUTE, linkIDAuthnResponse.getUserId() );
        httpSession.setAttribute( ATTRIBUTES_SESSION_ATTRIBUTE, linkIDAuthnResponse.getAttributes() );
        httpSession.setAttribute( PROTOCOL_SESSION_ATTRIBUTE, LinkIDProtocol.WS );
        httpSession.setAttribute( PROTOCOL_NO_REQUEST_SESSION_ATTRIBUTE, false );
        httpSession.setAttribute( PAYMENT_RESPONSE_SESSION_ATTRIBUTE, linkIDAuthnResponse.getPaymentResponse() );
        httpSession.setAttribute( EXTERNAL_CODE_RESPONSE_SESSION_ATTRIBUTE, linkIDAuthnResponse.getExternalCodeResponse() );
    }
}