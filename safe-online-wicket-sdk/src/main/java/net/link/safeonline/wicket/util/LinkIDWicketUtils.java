/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.util;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.util.wicket.util.WicketUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link LinkIDWicketUtils}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Sep 17, 2008</i> </p>
 *
 * @author lhunath
 */
public abstract class LinkIDWicketUtils {

    static final Log LOG = LogFactory.getLog( LinkIDWicketUtils.class );

    /**
     * Checks whether the user is logged in via the SafeOnline authentication web application or not.
     *
     * @return true if linkID user credentials are available.
     */
    public static boolean isLinkIDAuthenticated() {

        return LoginManager.isAuthenticated( WicketUtils.getHttpSession() );
    }

    /**
     * Gives back the SafeOnline authenticated userId, or <code>null</code> if the user was not yet authenticated.
     *
     * @return The linkID user identifier (possibly mapped to the application or subscription) of the user that has last authenticated
     *         himself.
     */
    public static String findLinkID() {

        return LoginManager.findUserId( WicketUtils.getHttpSession() );
    }

    /**
     * Gives back the SafeOnline authenticated device, or <code>null</code> if the user was not yet authenticated.
     *
     * @return The linkID devices with which the user that last authenticated himself had been authenticated.
     */
    public static List<String> findAuthenticatedDevices() {

        return LoginManager.findAuthenticatedDevices( WicketUtils.getHttpSession() );
    }

    /**
     * Gives back the SafeOnline attributes, or <code>null</code> if the user was not yet authenticated or no attributes were included.
     *
     * @return The linkID attributes that were sent with the last authentication response.
     */
    public static Map<String, List<AttributeSDK<?>>> findAttributes() {

        return LoginManager.findAttributes( WicketUtils.getHttpSession() );
    }

    /**
     * Gives back the optional certificate chain embedded in the signed authentication response (if applicable) or <code>null</code> if
     * not.
     *
     * @return the certificate chain in the last signed authentication response.
     */
    public static List<X509Certificate> findCertificateChain() {

        return LoginManager.findCertificateChain( WicketUtils.getHttpSession() );
    }

    /**
     * Removes the linkID credentials from the session.
     */
    public static void cleanup() {

        LoginManager.cleanup( WicketUtils.getHttpSession() );
    }
}
