/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.seam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.AuthnRequestFilter;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.util.servlet.SafeOnlineConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class for usage within a JBoss Seam JSF based web application.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineAuthenticationUtils {

    private static final Log                   LOG                                  = LogFactory.getLog(SafeOnlineAuthenticationUtils.class);

    /**
     * PATH to the servlet within olas-auth that initiates the authentication. <i>[required]</i>
     * 
     * <p>
     * We go here when the user begins an authentication from our application.
     * </p>
     */
    public static final String                 AUTH_SERVICE_PATH_CONTEXT_PARAM      = "AuthenticationServicePath";

    /**
     * PATH to the service within olas-auth that initiates the logout. <i>[required]</i>
     * 
     * <p>
     * We go here when the user begins a logout from our application.
     * </p>
     */
    public static final String                 LOGOUT_SERVICE_PATH_INIT_PARAM       = "LogoutServicePath";

    /**
     * PATH to the service within olas-auth that continues an SSO logout that was initiated by another application. <i>[required]</i>
     * 
     * <p>
     * We go here after olas-auth asked us to clean our session up following a logout request that was initiated from another application in
     * our application's SSO pool; such that olas-auth can continue this SSO logout process.
     * </p>
     */
    public static final String                 LOGOUT_EXIT_SERVICE_PATH_INIT_PARAM  = "LogoutExitServicePath";

    /**
     * PATH within our application to return to after a successful authentication that was initiated by the {@link AuthnRequestFilter}.
     * <i>[optional, default: The URL that the filter was triggered on]</i>
     */
    public static final String                 TARGET_INIT_PARAM                    = "Target";

    public static final String                 SKIP_LANDING_PAGE_INIT_PARAM         = "SkipLandingPage";

    /**
     * The application name that will be communicated towards the SafeOnline authentication web application. <i>[required]</i>
     */
    public static final String                 APPLICATION_NAME_CONTEXT_PARAM       = "ApplicationName";

    /**
     * The user-friendly application name that will be communicated towards the SafeOnline authentication web application. <i>[optional,
     * default: The application name]</i>
     */
    public static final String                 APPLICATION_FRIENDLY_NAME_INIT_PARAM = "ApplicationFriendlyName";

    /**
     * The authentication protocol used to begin the session with the OLAS authentication web application. <i>[optional]</i>
     * 
     * <ul>
     * <li>SAML2_BROWSER_POST <i>[default]</i></li>
     * </ul>
     */
    public static final String                 AUTHN_PROTOCOL_CONTEXT_PARAM         = "AuthenticationProtocol";

    /**
     * The authentication protocol may use the resource denoted by this value to digitally sign the authentication request. <i>[protocol
     * specific]</i>
     * 
     * <p>
     * The resource will be loaded using the context classloader.
     * </p>
     */
    public static final String                 KEY_STORE_RESOURCE_CONTEXT_PARAM     = "KeyStoreResource";

    /**
     * The authentication protocol may use the file denoted by this value to digitally sign the authentication request. <i>[protocol
     * specific]</i>
     * 
     * <p>
     * The value should be an absolute pathname in the file system.
     * </p>
     */
    public static final String                 KEY_STORE_FILE_CONTEXT_PARAM         = "KeyStoreFile";

    /**
     * The type of keystore denoted by the value of {@link #KEY_STORE_RESOURCE_CONTEXT_PARAM}. <i>[protocol specific]</i>
     * 
     * <ul>
     * <li>PKCS12</li>
     * <li>JKS</li>
     * </ul>
     */
    public static final String                 KEY_STORE_TYPE_CONTEXT_PARAM         = "KeyStoreType";

    /**
     * The password that unlocks the keystore and key entry specified by {@link #KEY_STORE_RESOURCE_CONTEXT_PARAM}. <i>[protocol
     * specific]</i>
     */
    public static final String                 KEY_STORE_PASSWORD_CONTEXT_PARAM     = "KeyStorePassword";

    /**
     * Determines whether single sign-on authentication should be used during the authentication request or not. <i>[optional]</i>
     * 
     * <ul>
     * <li>True <i>[default]</i></li>
     * <li>False</li>
     * </ul>
     */
    public static final String                 SINGLE_SIGN_ON_CONTEXT_PARAM         = "SingleSignOnEnabled";

    public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL               = AuthenticationProtocol.SAML2_BROWSER_POST;


    private SafeOnlineAuthenticationUtils() {

        // empty
    }

    /**
     * <p>
     * <b>Note: ONLY use this method from the JSF framework.</b>
     * </p>
     * 
     * @see #login(String, boolean, Locale, Integer, Boolean, Boolean, String, HttpServletRequest, HttpServletResponse)
     */
    public static String login(String target) {

        return login(target, false);
    }

    /**
     * <p>
     * <b>Note: ONLY use this method from the JSF framework.</b>
     * </p>
     * 
     * @see #login(String, boolean, Locale, Integer, Boolean, Boolean, String, HttpServletRequest, HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    public static String login(String target, boolean skipLandingPage) {

        return login(target, skipLandingPage, null, null, null, null, null);
    }

    /**
     * <p>
     * <b>Note: ONLY use this method from the JSF framework.</b>
     * </p>
     * 
     * @see #login(String, boolean, Locale, Integer, Boolean, Boolean, String, HttpServletRequest, HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    public static String login(String target, boolean skipLandingPage, Locale language, Integer color, Boolean minimal,
                               Boolean forceAuthentication, String session) {

        FacesContext context = FacesContext.getCurrentInstance();

        try {
            ExternalContext externalContext = context.getExternalContext();

            login(target, skipLandingPage, language, color, minimal, forceAuthentication, session,
                    (HttpServletRequest) externalContext.getRequest(), (HttpServletResponse) externalContext.getResponse());

            return null;
        }

        finally {
            // Signal the JavaServer Faces implementation that the HTTP response for this request has already been generated.
            // The JFS request lifecycle should be terminated as soon as the current phase is completed.
            context.responseComplete();
        }
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * 
     * @see #login(String, boolean, Locale, Integer, Boolean, Boolean, String, HttpServletRequest, HttpServletResponse)
     */
    public static void login(String target, HttpServletRequest request, HttpServletResponse response) {

        login(target, null, null, null, null, null, request, response);
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * 
     * @see #login(String, boolean, Locale, Integer, Boolean, Boolean, String, HttpServletRequest, HttpServletResponse)
     */
    public static void login(String target, Locale language, Integer color, Boolean minimal, Boolean forceAuthentication, String session,
                             HttpServletRequest request, HttpServletResponse response) {

        login(target, false, language, color, minimal, forceAuthentication, session, request, response);
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * 
     * Performs a SafeOnline login using the SafeOnline authentication web application.
     * 
     * <p>
     * The method uses:
     * <ul>
     * <li>{@link #AUTH_SERVICE_PATH_CONTEXT_PARAM}</li>
     * <li>{@link #APPLICATION_NAME_CONTEXT_PARAM}</li>
     * <li>{@link #AUTHN_PROTOCOL_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_RESOURCE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_FILE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_TYPE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_PASSWORD_CONTEXT_PARAM}</li>
     * <li>{@link #SINGLE_SIGN_ON_CONTEXT_PARAM}</li>
     * <li>{@link SafeOnlineAppConstants#COLOR_CONTEXT_PARAM}</li>
     * <li>{@link SafeOnlineAppConstants#MINIMAL_CONTEXT_PARAM}</li>
     * </ul>
     * </p>
     * 
     * @param target
     *            The target to which to redirect to after successful authentication. If not absolute, the web application's base URL will
     *            be prefixed to it.
     * @param language
     *            The language to use in the OLAS application.
     * @param color
     *            The 24-bit color override {@link SafeOnlineAppConstants#COLOR_CONTEXT_PARAM} with. <code>null</code> prevents overriding.
     * @param minimal
     *            The value to override {@link SafeOnlineAppConstants#MINIMAL_CONTEXT_PARAM} with. <code>null</code> prevents overriding.
     * @param forceAuthentication
     *            Force an authentication and do not allow SSO for this particular login ( even tho the application can be SSO enabled )
     */
    public static void login(String target, boolean skipLandingPage, Locale language, Integer color, Boolean minimal,
                             Boolean forceAuthentication, String session, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> config = new HashMap<String, String>();
        ServletContext context = request.getSession().getServletContext();

        @SuppressWarnings("unchecked")
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            config.put(name, context.getInitParameter(name));
        }

        /* Initialize parameters from web.xml */
        String authenticationServicePath = getInitParameter(config, AUTH_SERVICE_PATH_CONTEXT_PARAM);
        String applicationName = getInitParameter(config, APPLICATION_NAME_CONTEXT_PARAM);
        String applicationFriendlyName = getInitParameter(config, APPLICATION_FRIENDLY_NAME_INIT_PARAM, null);
        String authenticationProtocolString = getInitParameter(config, AUTHN_PROTOCOL_CONTEXT_PARAM, DEFAULT_AUTHN_PROTOCOL.name());
        String keyStoreResource = getInitParameter(config, KEY_STORE_RESOURCE_CONTEXT_PARAM, null);
        String keyStoreFile = getInitParameter(config, KEY_STORE_FILE_CONTEXT_PARAM, null);
        String keyStorePassword = getInitParameter(config, KEY_STORE_PASSWORD_CONTEXT_PARAM, null);
        String keyStoreType = getInitParameter(config, KEY_STORE_TYPE_CONTEXT_PARAM, null);
        String ssoEnabledString = getInitParameter(config, SINGLE_SIGN_ON_CONTEXT_PARAM, null);

        authenticationServicePath = SafeOnlineConfig.authbase() + authenticationServicePath;

        LOG.debug("redirecting to: " + authenticationServicePath);

        /* Figure out what protocol to use. */
        AuthenticationProtocol authenticationProtocol = null;
        try {
            authenticationProtocol = AuthenticationProtocol.toAuthenticationProtocol(authenticationProtocolString);
        } catch (UnavailableException e) {
            throw new RuntimeException("could not parse authentication protocol: " + authenticationProtocolString);
        }
        LOG.debug("authentication protocol: " + authenticationProtocol);

        /*
         * Single Sign On Enabled or not
         */
        boolean ssoEnabled = true;
        if (null != ssoEnabledString) {
            ssoEnabled = Boolean.parseBoolean(ssoEnabledString);
        }
        LOG.debug("single sign-on enabled: " + ssoEnabled);

        /* Load key data if provided. */
        KeyPair keyPair = null;
        X509Certificate certificate = null;
        PrivateKeyEntry privateKeyEntry = getApplicationKey(keyStoreFile, keyStoreResource, keyStoreType, keyStorePassword);
        if (privateKeyEntry != null) {
            keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            certificate = (X509Certificate) privateKeyEntry.getCertificate();
        }

        /*
         * Use encodeRedirectURL to add parameters to it that should help preserve the session upon return from SafeOnline auth should the
         * browser not support cookies.
         */
        String targetUrl = response.encodeRedirectURL(SafeOnlineConfig.absoluteApplicationUrlFromPath(request, target));
        LOG.debug("target url: " + targetUrl);

        /* Initialize and execute the authentication protocol. */
        try {
            AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol, authenticationServicePath,
                    applicationName, applicationFriendlyName, keyPair, certificate, forceAuthentication, config, request);
            LOG.debug("initialized protocol");
        } catch (ServletException e) {
            throw new RuntimeException("could not init authentication protocol handler: " + authenticationProtocol + "; original message: "
                    + e.getMessage(), e);
        }

        // Defaults for color & minimal from web.xml init params.
        Integer authColor = color;
        if (authColor == null) {
            String colorConfig = config.get(SafeOnlineAppConstants.COLOR_CONTEXT_PARAM);
            if (colorConfig != null && colorConfig.length() > 0) {
                authColor = Integer.decode(colorConfig);
            }
        }
        Boolean authMinimal = minimal;
        if (authMinimal == null) {
            String minimalConfig = config.get(SafeOnlineAppConstants.MINIMAL_CONTEXT_PARAM);
            if (minimalConfig != null && minimalConfig.length() > 0) {
                authMinimal = Boolean.parseBoolean(minimalConfig);
            }
        }

        // Initiate the authentication.
        try {
            AuthenticationProtocolManager.initiateAuthentication(request, response, targetUrl, skipLandingPage, language, authColor,
                    authMinimal, session);
            LOG.debug("executed protocol");
        } catch (Exception e) {
            throw new RuntimeException("could not initiate authentication: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * <b>Note: ONLY use this method from the JSF framework.</b>
     * </p>
     * 
     * @see #logout(String, String,String, HttpServletRequest, HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    public static boolean logout(String subjectName, String target) {

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        try {
            return logout(subjectName, target, null, (HttpServletRequest) externalContext.getRequest(),
                    (HttpServletResponse) externalContext.getResponse());
        }

        finally {
            // Signal the JavaServer Faces implementation that the HTTP response for this request has already been generated.
            // The JFS request lifecycle should be terminated as soon as the current phase is completed.
            context.responseComplete();
        }
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * 
     * @see #logout(String, String, String, HttpServletRequest, HttpServletResponse)
     */
    public static boolean logout(String target, String session, HttpServletRequest request, HttpServletResponse response) {

        try {
            return logout(LoginManager.getUserId(request), target, session, request, response);
        } catch (ServletException e) {
            LOG.warn("Couldn't find user id of logged in user; not logged in?", e);
            return false;
        }
    }

    /**
     * <p>
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * </p>
     * 
     * Performs a SafeOnline logout using the SafeOnline authentication web application.
     * 
     * <p>
     * The method uses:
     * <ul>
     * <li>{@link #LOGOUT_SERVICE_PATH_INIT_PARAM}</li>
     * <li>{@link #APPLICATION_NAME_CONTEXT_PARAM}</li>
     * <li>{@link #AUTHN_PROTOCOL_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_RESOURCE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_FILE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_TYPE_CONTEXT_PARAM}</li>
     * <li>{@link #KEY_STORE_PASSWORD_CONTEXT_PARAM}</li>
     * </ul>
     * 
     * @param subjectName
     *            The user ID of the subject logging out.
     * 
     * @param target
     *            The target to which to redirect to after successful logout. If not absolute, the web application's base URL will be
     *            prefixed to it.
     * 
     * @param session
     *            The optional SSO session to logout
     * 
     * @return <code>true</code> if redirected successful, <code>false</code> if not redirected ( e.g. when SSO is disabled )
     */
    public static boolean logout(String subjectName, String target, String session, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> config = new HashMap<String, String>();
        ServletContext context = request.getSession().getServletContext();

        @SuppressWarnings("unchecked")
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            config.put(name, context.getInitParameter(name));
        }

        /* Initialize parameters from web.xml */
        String logoutServicePath = getInitParameter(config, LOGOUT_SERVICE_PATH_INIT_PARAM);
        String applicationName = getInitParameter(config, APPLICATION_NAME_CONTEXT_PARAM);
        String applicationFriendlyName = getInitParameter(config, APPLICATION_FRIENDLY_NAME_INIT_PARAM, null);
        String authenticationProtocolString = getInitParameter(config, AUTHN_PROTOCOL_CONTEXT_PARAM, DEFAULT_AUTHN_PROTOCOL.name());
        String keyStoreResource = getInitParameter(config, KEY_STORE_RESOURCE_CONTEXT_PARAM, null);
        String keyStoreFile = getInitParameter(config, KEY_STORE_FILE_CONTEXT_PARAM, null);
        String keyStorePassword = getInitParameter(config, KEY_STORE_PASSWORD_CONTEXT_PARAM, null);
        String keyStoreType = getInitParameter(config, KEY_STORE_TYPE_CONTEXT_PARAM, null);
        String ssoEnabledString = getInitParameter(config, SINGLE_SIGN_ON_CONTEXT_PARAM, null);

        logoutServicePath = SafeOnlineConfig.authbase() + logoutServicePath;

        LOG.debug("redirecting to: " + logoutServicePath);

        /* Figure out what protocol to use. */
        AuthenticationProtocol authenticationProtocol = null;
        try {
            authenticationProtocol = AuthenticationProtocol.toAuthenticationProtocol(authenticationProtocolString);
        } catch (UnavailableException e) {
            throw new RuntimeException("could not parse authentication protocol: " + authenticationProtocolString);
        }
        LOG.debug("authentication protocol: " + authenticationProtocol);

        /*
         * Single Sign On Enabled or not. If not, single logout does not make much sense.
         */
        boolean ssoEnabled = true;
        if (null != ssoEnabledString) {
            ssoEnabled = Boolean.parseBoolean(ssoEnabledString);
        }
        LOG.debug("single sign-on enabled: " + ssoEnabled);
        if (false == ssoEnabled)
            return false;

        /* Load key data if provided. */
        KeyPair keyPair = null;
        X509Certificate certificate = null;
        PrivateKeyEntry privateKeyEntry = getApplicationKey(keyStoreFile, keyStoreResource, keyStoreType, keyStorePassword);
        if (privateKeyEntry != null) {
            keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            certificate = (X509Certificate) privateKeyEntry.getCertificate();
        }

        /*
         * Use encodeRedirectURL to add parameters to it that should help preserve the session upon return from SafeOnline auth should the
         * browser not support cookies.
         */
        String targetUrl = response.encodeRedirectURL(SafeOnlineConfig.absoluteApplicationUrlFromPath(request, target));
        LOG.debug("target url: " + targetUrl);

        /* Initialize and execute the authentication protocol. */
        try {
            AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol, logoutServicePath, applicationName,
                    applicationFriendlyName, keyPair, certificate, ssoEnabled, config, request);
            LOG.debug("initialized protocol");
        } catch (ServletException e) {
            throw new RuntimeException("could not init authentication protocol handler: " + authenticationProtocol + "; original message: "
                    + e.getMessage(), e);
        }
        try {
            AuthenticationProtocolManager.initiateLogout(request, response, targetUrl, subjectName, session);
            LOG.debug("executed protocol");
        } catch (Exception e) {
            throw new RuntimeException("could not initiate logout: " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * Load the application key from the given key store file OR resource (at least one must be <code>null</code>).
     * 
     * @param keyStorePassword
     * @param keyStoreType
     */
    private static PrivateKeyEntry getApplicationKey(String keyStoreFile, String keyStoreResource, String keyStoreType,
                                                     String keyStorePassword) {

        if (null == keyStoreResource && null == keyStoreFile)
            return null;

        /* Can't have both resource and file defined. */
        if (null != keyStoreResource && null != keyStoreFile)
            throw new RuntimeException("both KeyStoreResource and KeyStoreFile are defined");

        InputStream keyStoreInputStream;
        if (null != keyStoreResource) {
            keyStoreInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreResource);
            if (null == keyStoreInputStream)
                throw new RuntimeException("resource not found: " + keyStoreResource);
        } else {
            try {
                keyStoreInputStream = new FileInputStream(keyStoreFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("file not found: " + keyStoreFile);
            }
        }

        return KeyStoreUtils.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream, keyStorePassword, keyStorePassword);
    }

    private static String getInitParameter(Map<String, String> config, String parameterName) {

        if (!config.containsKey(parameterName))
            throw new RuntimeException("missing context-param in web.xml: " + parameterName);

        return config.get(parameterName);
    }

    private static String getInitParameter(Map<String, String> config, String parameterName, String defaultValue) {

        if (config.containsKey(parameterName))
            return config.get(parameterName);

        return defaultValue;
    }
}
