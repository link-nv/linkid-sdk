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
import net.link.safeonline.attribute.AttributeSDK;
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

    public static final String USERID_SESSION_ATTRIBUTE                = LogManager.class.getName() + ".userId";
    public static final String AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE = LogManager.class.getName() + ".authenticatedDevices";
    public static final String ATTRIBUTES_SESSION_ATTRIBUTE            = LogManager.class.getName() + ".attributes";
    public static final String CERTIFCATE_CHAIN_SESSION_ATTRIBUTE      = LogManager.class.getName() + ".certificateChain";

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
     * Cleanup linkId session attributes.
     *
     * @param httpSession The session from which the credentials will be removed.
     */
    public static void cleanup(HttpSession httpSession) {

        httpSession.removeAttribute( USERID_SESSION_ATTRIBUTE );
        cleanupAttributes( httpSession );
        httpSession.removeAttribute( AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE );
        httpSession.removeAttribute( CERTIFCATE_CHAIN_SESSION_ATTRIBUTE );
    }

    /**
     * This method is invoked by the SDK after handling an authentication response to make the authentication data available to the
     * application.
     *
     * @param httpSession          The session on which the credentials will be made available.
     * @param userId               the userId of the SafeOnline authenticated principal.
     * @param attributes           the attributes from the identity of the authenticated application.
     * @param authenticatedDevices the devices the SafeOnline authenticated principal used to authenticate with.
     * @param certificateChain     the certificate chain optionally present if response was signed and contained it embedded
     */
    public static void set(HttpSession httpSession, String userId, Map<String, List<AttributeSDK<?>>> attributes,
                           List<String> authenticatedDevices, CertificateChain certificateChain) {

        httpSession.setAttribute( USERID_SESSION_ATTRIBUTE, userId );
        httpSession.setAttribute( ATTRIBUTES_SESSION_ATTRIBUTE, attributes );
        httpSession.setAttribute( AUTHENTICATED_DEVICES_SESSION_ATTRIBUTE, authenticatedDevices );
        httpSession.setAttribute( CERTIFCATE_CHAIN_SESSION_ATTRIBUTE, certificateChain );
    }
}
