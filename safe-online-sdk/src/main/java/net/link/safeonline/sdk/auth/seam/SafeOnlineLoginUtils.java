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
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class for usage within a JBoss Seam JSF based web application.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineLoginUtils {

    private static final Log                   LOG                                  = LogFactory
                                                                                            .getLog(SafeOnlineLoginUtils.class);

    public static final String                 AUTH_SERVICE_URL_INIT_PARAM          = "AuthenticationServiceUrl";
    public static final String                 TARGET_BASE_URL_INIT_PARAM           = "TargetBaseUrl";
    public static final String                 APPLICATION_NAME_INIT_PARAM          = "ApplicationName";
    public static final String                 APPLICATION_FRIENDLY_NAME_INIT_PARAM = "ApplicationFriendlyName";
    public static final String                 AUTHN_PROTOCOL_INIT_PARAM            = "AuthenticationProtocol";
    public static final String                 KEY_STORE_RESOURCE_INIT_PARAM        = "KeyStoreResource";
    public static final String                 KEY_STORE_FILE_INIT_PARAM            = "KeyStoreFile";
    public static final String                 KEY_STORE_TYPE_INIT_PARAM            = "KeyStoreType";
    public static final String                 KEY_STORE_PASSWORD_INIT_PARAM        = "KeyStorePassword";
    public static final String                 SINGLE_SIGN_ON_INIT_PARAM            = "SingleSignOnEnabled";

    public static final String                 LOGOUT_SERVICE_URL_INIT_PARAM        = "LogoutServiceUrl";
    public static final String                 LOGOUT_EXIT_SERVICE_URL_INIT_PARAM   = "LogoutExitServiceUrl";

    public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL               = AuthenticationProtocol.SAML2_BROWSER_POST;


    private SafeOnlineLoginUtils() {

        // empty
    }

    /**
     * Performs a SafeOnline login using the SafeOnline authentication web application.
     * 
     * <b>Note: This method is ONLY for logging in from an application that uses the JSF framework.</b>
     * 
     * <p>
     * The method requires the <code>AuthenticationServiceUrl</code> context parameter defined in <code>web.xml</code>
     * pointing to the location of the SafeOnline authentication web application.
     * </p>
     * 
     * <p>
     * The method also requires the <code>TargetBaseUrl</code> context parameter defined in <code>web.xml</code>
     * pointing to the base location to redirect to after successful authentication.
     * </p>
     * 
     * <p>
     * The method also requires the <code>ApplicationName</code> context parameter defined in <code>web.xml</code>
     * containing the application name that will be communicated towards the SafeOnline authentication web application.
     * </p>
     * 
     * <p>
     * The method also requires the <code>AuthenticationProtocol</code> context parameter defined in
     * <code>web.xml</code> containing the authentication protocol used between the application and the OLAS
     * authentication web application. This can be: SAML2_BROWSER_POST. Defaults to: SAML2_BROWSER_POST
     * </p>
     * 
     * <p>
     * The optional keystore resource name <code>KeyStoreResource</code> context parameter. The key pair within this
     * keystore can be used by the authentication protocol handler to digitally sign the authentication request.
     * </p>
     * 
     * <p>
     * The optional keystore file name <code>KeyStoreFile</code> context parameter. The key pair within this keystore
     * can be used by the authentication protocol handler to digitally sign the authentication request.
     * </p>
     * 
     * <p>
     * The optional <code>KeyStoreType</code> key store type context parameter. Accepted values are: <code>pkcs12</code>
     * and <code>jks</code>.
     * </p>
     * 
     * <p>
     * The optional <code>KeyStorePassword</code> context parameter contains the password to unlock the keystore and key
     * entry.
     * </p>
     * 
     * <p>
     * The optional <code>SingleSignOnEnabled</code> init parameter specified whether single sign-on can be used or not.
     * Accepted values are: <code>true</code> or <code>false</code>. If omitted, single sign-on will be enabled by
     * default.
     * </p>
     * 
     * @param target
     *            The target to which to redirect to after successful authentication. Don't put the full URL in here,
     *            the full URL is retrieved with the {@link #TARGET_BASE_URL_INIT_PARAM}.
     * 
     */
    @SuppressWarnings("unchecked")
    public static String login(String target) {

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        try {
            return login(target, externalContext.getInitParameterMap(), (HttpServletRequest) externalContext
                    .getRequest(), (HttpServletResponse) externalContext.getResponse());
        }

        finally {
            /*
             * Signal the JavaServer Faces implementation that the HTTP response for this request has already been
             * generated (such as an HTTP redirect), and that the request processing lifecycle should be terminated as
             * soon as the current phase is completed.
             */
            context.responseComplete();
        }
    }

    /**
     * Performs a SafeOnline login using the SafeOnline authentication web application.
     * 
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * 
     * @see #login(String) For details about the init parameters that should be configured in the application's
     *      <code>web.xml</code>.
     * 
     * @param target
     *            The target url to redirect to after successful authentication.
     * 
     * @param request
     *            The {@link HttpServletRequest} object from the servlet making the login request.
     * @param response
     *            The {@link HttpServletResponse} object from the servlet making the login request.
     */
    public static String login(String target, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> config = new HashMap<String, String>();
        ServletContext context = request.getSession().getServletContext();

        @SuppressWarnings("unchecked")
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            config.put(name, context.getInitParameter(name));
        }

        return login(target, config, request, response);
    }

    private static String login(String target, Map<String, String> config, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        /* Initialize parameters from web.xml */
        String authenticationServiceUrl = getInitParameter(config, AUTH_SERVICE_URL_INIT_PARAM);
        String targetBaseUrl = getInitParameter(config, TARGET_BASE_URL_INIT_PARAM);
        String applicationName = getInitParameter(config, APPLICATION_NAME_INIT_PARAM);
        String applicationFriendlyName = getInitParameter(config, APPLICATION_FRIENDLY_NAME_INIT_PARAM, null);
        String authenticationProtocolString = getInitParameter(config, AUTHN_PROTOCOL_INIT_PARAM,
                DEFAULT_AUTHN_PROTOCOL.name());
        String keyStoreResource = getInitParameter(config, KEY_STORE_RESOURCE_INIT_PARAM, null);
        String keyStoreFile = getInitParameter(config, KEY_STORE_FILE_INIT_PARAM, null);
        String keyStorePassword = getInitParameter(config, KEY_STORE_PASSWORD_INIT_PARAM, null);
        String keyStoreType = getInitParameter(config, KEY_STORE_TYPE_INIT_PARAM, null);
        String ssoEnabledString = getInitParameter(config, SINGLE_SIGN_ON_INIT_PARAM, null);
        LOG.debug("redirecting to: " + authenticationServiceUrl);

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
        PrivateKeyEntry privateKeyEntry = getApplicationKey(keyStoreFile, keyStoreResource, keyStoreType,
                keyStorePassword);
        if (privateKeyEntry != null) {
            keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            certificate = (X509Certificate) privateKeyEntry.getCertificate();
        }

        /*
         * Use encodeRedirectURL to add parameters to it that should help preserve the session upon return from
         * SafeOnline auth should the browser not support cookies.
         */
        String targetUrl = targetBaseUrl + target;
        targetUrl = httpResponse.encodeRedirectURL(targetUrl);
        LOG.debug("target url: " + targetUrl);

        /* Initialize and execute the authentication protocol. */
        try {
            AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol,
                    authenticationServiceUrl, applicationName, applicationFriendlyName, keyPair, certificate,
                    ssoEnabled, config, httpRequest);
            LOG.debug("initialized protocol");
        } catch (ServletException e) {
            throw new RuntimeException("could not init authentication protocol handler: " + authenticationProtocol
                    + "; original message: " + e.getMessage(), e);
        }
        try {
            AuthenticationProtocolManager.initiateAuthentication(httpRequest, httpResponse, targetUrl);
            LOG.debug("executed protocol");
        } catch (Exception e) {
            throw new RuntimeException("could not initiate authentication: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Performs a SafeOnline single logout using the SafeOnline authentication web application.
     * 
     * <b>Note: This method is ONLY for logging in from an application that uses the JSF framework.</b>
     * 
     * <p>
     * The method requires the <code>LogoutServiceUrl</code> context parameter defined in <code>web.xml</code> pointing
     * to the location of the SafeOnline authentication web application logout entry point.
     * </p>
     * 
     * <p>
     * The method also requires the <code>TargetBaseUrl</code> context parameter defined in <code>web.xml</code>
     * pointing to the base location to redirect to after successful logout.
     * </p>
     * 
     * <p>
     * The method also requires the <code>ApplicationName</code> context parameter defined in <code>web.xml</code>
     * containing the application name that will be communicated towards the SafeOnline authentication web application.
     * </p>
     * 
     * <p>
     * The method also requires the <code>AuthenticationProtocol</code> context parameter defined in
     * <code>web.xml</code> containing the authentication protocol used between the application and the OLAS
     * authentication web application. This can be: SAML2_BROWSER_POST. Defaults to: SAML2_BROWSER_POST
     * </p>
     * 
     * <p>
     * The optional keystore resource name <code>KeyStoreResource</code> context parameter. The key pair within this
     * keystore can be used by the authentication protocol handler to digitally sign the authentication request.
     * </p>
     * 
     * <p>
     * The optional keystore file name <code>KeyStoreFile</code> context parameter. The key pair within this keystore
     * can be used by the authentication protocol handler to digitally sign the authentication request.
     * </p>
     * 
     * <p>
     * The optional <code>KeyStoreType</code> key store type context parameter. Accepted values are: <code>pkcs12</code>
     * and <code>jks</code>.
     * </p>
     * 
     * <p>
     * The optional <code>KeyStorePassword</code> context parameter contains the password to unlock the keystore and key
     * entry.
     * </p>
     * 
     * @param subjectName
     *            the user ID of the subject logging out.
     * 
     * @param target
     *            The target to which to redirect to after successful logout. Don't put the full URL in here, the full
     *            URL is retrieved with the {@link #TARGET_BASE_URL_INIT_PARAM}.
     */
    @SuppressWarnings("unchecked")
    public static void logout(String subjectName, String target) {

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        try {
            logout(subjectName, target, externalContext.getInitParameterMap(), (HttpServletRequest) externalContext
                    .getRequest(), (HttpServletResponse) externalContext.getResponse());
        }

        finally {
            /*
             * Signal the JavaServer Faces implementation that the HTTP response for this request has already been
             * generated (such as an HTTP redirect), and that the request processing lifecycle should be terminated as
             * soon as the current phase is completed.
             */
            context.responseComplete();
        }
    }

    /**
     * Performs a SafeOnline logout using the SafeOnline authentication web application.
     * 
     * <b>Note: This is a general purpose method that should work for any web application framework.</b>
     * 
     * @see #logout(String, String) For details about the init parameters that should be configured in the application's
     *      <code>web.xml</code>.
     * 
     * @param target
     *            The target url to redirect to after successful logout.
     * @param request
     *            The {@link HttpServletRequest} object from the servlet making the login request.
     * @param response
     *            The {@link HttpServletResponse} object from the servlet making the login request.
     */
    public static void logout(String target, HttpServletRequest request, HttpServletResponse response) {

        Map<String, String> config = new HashMap<String, String>();
        ServletContext context = request.getSession().getServletContext();

        @SuppressWarnings("unchecked")
        Enumeration<String> names = context.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            config.put(name, context.getInitParameter(name));
        }

        try {
            logout(LoginManager.getUserId(request), target, config, request, response);
        } catch (ServletException e) {
            // Not logged in.
        }
    }

    private static void logout(String subjectName, String target, Map<String, String> config,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        /* Initialize parameters from web.xml */
        String logoutServiceUrl = getInitParameter(config, LOGOUT_SERVICE_URL_INIT_PARAM);
        String targetBaseUrl = getInitParameter(config, TARGET_BASE_URL_INIT_PARAM);
        String applicationName = getInitParameter(config, APPLICATION_NAME_INIT_PARAM);
        String applicationFriendlyName = getInitParameter(config, APPLICATION_FRIENDLY_NAME_INIT_PARAM, null);
        String authenticationProtocolString = getInitParameter(config, AUTHN_PROTOCOL_INIT_PARAM,
                DEFAULT_AUTHN_PROTOCOL.name());
        String keyStoreResource = getInitParameter(config, KEY_STORE_RESOURCE_INIT_PARAM, null);
        String keyStoreFile = getInitParameter(config, KEY_STORE_FILE_INIT_PARAM, null);
        String keyStorePassword = getInitParameter(config, KEY_STORE_PASSWORD_INIT_PARAM, null);
        String keyStoreType = getInitParameter(config, KEY_STORE_TYPE_INIT_PARAM, null);
        String ssoEnabledString = getInitParameter(config, SINGLE_SIGN_ON_INIT_PARAM, null);
        LOG.debug("redirecting to: " + logoutServiceUrl);

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
            return;

        /* Load key data if provided. */
        KeyPair keyPair = null;
        X509Certificate certificate = null;
        PrivateKeyEntry privateKeyEntry = getApplicationKey(keyStoreFile, keyStoreResource, keyStoreType,
                keyStorePassword);
        if (privateKeyEntry != null) {
            keyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            certificate = (X509Certificate) privateKeyEntry.getCertificate();
        }

        /*
         * Use encodeRedirectURL to add parameters to it that should help preserve the session upon return from
         * SafeOnline auth should the browser not support cookies.
         */
        String targetUrl = targetBaseUrl + target;
        targetUrl = httpResponse.encodeRedirectURL(targetUrl);
        LOG.debug("target url: " + targetUrl);

        /* Initialize and execute the authentication protocol. */
        try {
            AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol, logoutServiceUrl,
                    applicationName, applicationFriendlyName, keyPair, certificate, ssoEnabled, config, httpRequest);
            LOG.debug("initialized protocol");
        } catch (ServletException e) {
            throw new RuntimeException("could not init authentication protocol handler: " + authenticationProtocol
                    + "; original message: " + e.getMessage(), e);
        }
        try {
            AuthenticationProtocolManager.initiateLogout(httpRequest, httpResponse, targetUrl, subjectName);
            LOG.debug("executed protocol");
        } catch (Exception e) {
            throw new RuntimeException("could not initiate logout: " + e.getMessage(), e);
        }
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
        if (null != keyStoreResource && null != keyStoreFile) {
            throw new RuntimeException("both KeyStoreResource and KeyStoreFile are defined");
        }

        InputStream keyStoreInputStream;
        if (null != keyStoreResource) {
            keyStoreInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreResource);
            if (null == keyStoreInputStream) {
                throw new RuntimeException("resource not found: " + keyStoreResource);
            }
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

        if (!config.containsKey(parameterName)) {
            throw new RuntimeException("missing context-param in web.xml: " + parameterName);
        }

        return config.get(parameterName);
    }

    private static String getInitParameter(Map<String, String> config, String parameterName, String defaultValue) {

        if (config.containsKey(parameterName))
            return config.get(parameterName);

        return defaultValue;
    }
}
