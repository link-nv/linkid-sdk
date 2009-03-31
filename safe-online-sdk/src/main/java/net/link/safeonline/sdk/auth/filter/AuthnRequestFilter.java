/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.AuthenticationProtocolManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;
import net.link.safeonline.util.servlet.AbstractInjectionFilter;
import net.link.safeonline.util.servlet.SafeOnlineConfig;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * SafeOnline Authentication Request Filter. This filter can be used by servlet container based web applications for authentication via
 * SafeOnline. This filter initiates the authentication request towards the SafeOnline authentication web application. The handling of the
 * authentication response is done via the {@link AuthnResponseFilter}.
 * 
 * <p>
 * The configuration of this filter should be managed via the <code>web.xml</code> deployment descriptor.
 * </p>
 * 
 * <ul>
 * <li>{@link SafeOnlineLoginUtils#AUTH_SERVICE_PATH_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#APPLICATION_NAME_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#AUTHN_PROTOCOL_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#TARGET_INIT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#KEY_STORE_RESOURCE_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#KEY_STORE_FILE_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#KEY_STORE_TYPE_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#KEY_STORE_PASSWORD_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineLoginUtils#SINGLE_SIGN_ON_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineAppConstants#COLOR_CONTEXT_PARAM}</li>
 * <li>{@link SafeOnlineAppConstants#MINIMAL_CONTEXT_PARAM}</li>
 * </ul>
 * 
 * <p>
 * If an application wishes to communicate to the OLAS authentication webapp the language to be used, the session parameter
 * <code>Language</code> needs to be set containing as value the {@link Locale} of the language.
 * </p>
 * 
 * @author fcorneli
 * @see AuthnResponseFilter
 */
public class AuthnRequestFilter extends AbstractInjectionFilter {

    private static final Log                   LOG                    = LogFactory.getLog(AuthnRequestFilter.class);

    public static final String                 LANGUAGE_SESSION_PARAM = "Language";

    public static final String                 SESSION_SESSION_PARAM  = "Session";

    public static final AuthenticationProtocol DEFAULT_AUTHN_PROTOCOL = AuthenticationProtocol.SAML2_BROWSER_POST;

    @Init(name = SafeOnlineLoginUtils.AUTH_SERVICE_PATH_CONTEXT_PARAM)
    private String                             authenticationServicePath;

    @Init(name = SafeOnlineLoginUtils.TARGET_INIT_PARAM, optional = true, checkContext = false)
    private String                             target;

    private String                             targetUrl;

    @Init(name = SafeOnlineLoginUtils.SKIP_LANDING_PAGE_INIT_PARAM, optional = true, checkContext = false)
    private String                             skipLandingPageString;
    private boolean                            skipLandingPage;

    @Init(name = SafeOnlineLoginUtils.APPLICATION_NAME_CONTEXT_PARAM)
    private String                             applicationName;

    @Init(name = SafeOnlineLoginUtils.APPLICATION_FRIENDLY_NAME_INIT_PARAM, optional = true)
    private String                             applicationFriendlyName;

    @Init(name = SafeOnlineLoginUtils.AUTHN_PROTOCOL_CONTEXT_PARAM, optional = true)
    private String                             authenticationProtocolString;

    private AuthenticationProtocol             authenticationProtocol;

    @Init(name = SafeOnlineLoginUtils.KEY_STORE_RESOURCE_CONTEXT_PARAM, optional = true)
    private String                             p12KeyStoreResourceName;

    @Init(name = SafeOnlineLoginUtils.KEY_STORE_FILE_CONTEXT_PARAM, optional = true)
    private String                             p12KeyStoreFileName;

    @Init(name = SafeOnlineLoginUtils.KEY_STORE_PASSWORD_CONTEXT_PARAM)
    private String                             keyStorePassword;

    @Init(name = SafeOnlineLoginUtils.KEY_STORE_TYPE_CONTEXT_PARAM, defaultValue = "pkcs12")
    private String                             keyStoreType;

    @Init(name = SafeOnlineLoginUtils.SINGLE_SIGN_ON_CONTEXT_PARAM, optional = true)
    private String                             ssoEnabledString;
    private boolean                            ssoEnabled;

    @Init(name = SafeOnlineAppConstants.COLOR_CONTEXT_PARAM, optional = true)
    private String                             colorConfig;
    private Integer                            authColor;

    @Init(name = SafeOnlineAppConstants.MINIMAL_CONTEXT_PARAM, optional = true)
    private String                             minimalConfig;
    private Boolean                            authMinimal;

    private KeyPair                            applicationKeyPair;

    private X509Certificate                    applicationCertificate;


    @Override
    public void init(FilterConfig config)
            throws ServletException {

        super.init(config);
        LOG.debug("init");
        if (null == authenticationProtocolString) {
            authenticationProtocol = DEFAULT_AUTHN_PROTOCOL;
        } else {
            authenticationProtocol = AuthenticationProtocol.toAuthenticationProtocol(authenticationProtocolString);
        }
        LOG.debug("authentication protocol: " + authenticationProtocol);

        if (null == ssoEnabledString) {
            ssoEnabled = true;
        } else {
            ssoEnabled = Boolean.parseBoolean(ssoEnabledString);
        }
        LOG.debug("single sign-on enabled: " + ssoEnabled);

        if (null == skipLandingPageString) {
            skipLandingPage = false;
        } else {
            skipLandingPage = Boolean.parseBoolean(skipLandingPageString);
        }

        // Defaults for color & minimal from web.xml init params.
        if (colorConfig != null && colorConfig.length() > 0) {
            authColor = Integer.decode(colorConfig);
        }
        if (minimalConfig != null && minimalConfig.length() > 0) {
            authMinimal = Boolean.parseBoolean(minimalConfig);
        }

        InputStream keyStoreInputStream = null;
        if (null != p12KeyStoreResourceName) {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            LOG.debug("classloader name: " + classLoader.getClass().getName());
            keyStoreInputStream = classLoader.getResourceAsStream(p12KeyStoreResourceName);
            if (null == keyStoreInputStream)
                throw new UnavailableException("PKCS12 keystore resource not found: " + p12KeyStoreResourceName);
        } else if (null != p12KeyStoreFileName) {
            try {
                keyStoreInputStream = new FileInputStream(p12KeyStoreFileName);
            } catch (FileNotFoundException e) {
                throw new UnavailableException("PKCS12 keystore resource not found: " + p12KeyStoreFileName);
            }
        }
        if (null != keyStoreInputStream) {
            PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream, keyStorePassword,
                    keyStorePassword);
            applicationKeyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            applicationCertificate = (X509Certificate) privateKeyEntry.getCertificate();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        LOG.debug("doFilter");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        boolean loggedIn = LoginManager.isAuthenticated(httpRequest);
        if (false == loggedIn) {
            initiateAuthentication(httpRequest, httpResponse);
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private void initiateAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        AuthenticationProtocolManager.createAuthenticationProtocolHandler(authenticationProtocol, SafeOnlineConfig.authbase()
                + authenticationServicePath, applicationName, applicationFriendlyName, applicationKeyPair, applicationCertificate,
                ssoEnabled, configParams, request);

        // Use encodeRedirectURL to add the session ID to the target URL (preserve session for browsers that don't support cookies).
        if (null != target) {
            targetUrl = target;
            if (!URI.create(targetUrl).isAbsolute()) {
                targetUrl = response.encodeRedirectURL(SafeOnlineConfig.absoluteApplicationUrlFromPath(request, target));
            }

            LOG.debug("target url: " + targetUrl);
        }

        Locale language = null;
        if (null != request.getAttribute(LANGUAGE_SESSION_PARAM)) {
            language = (Locale) request.getAttribute(LANGUAGE_SESSION_PARAM);
        }

        String session = null;
        if (null != request.getAttribute(SESSION_SESSION_PARAM)) {
            session = (String) request.getAttribute(SESSION_SESSION_PARAM);
        }

        AuthenticationProtocolManager.initiateAuthentication(request, response, targetUrl, skipLandingPage, language, authColor,
                authMinimal, session);
    }

    public void destroy() {

        LOG.debug("destroy");
    }
}
