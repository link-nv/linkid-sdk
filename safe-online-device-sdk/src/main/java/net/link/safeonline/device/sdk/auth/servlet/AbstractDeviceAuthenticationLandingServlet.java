/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.device.sdk.auth.saml2.Saml2Handler;
import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.ServletUtils;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractDeviceAuthenticationLandingServlet extends AbstractInjectionServlet {

    /**
     * PATH within the web application that performs the device authentication. <i>[required]</i>
     */
    public static final String AUTHENTICATION_PATH = "AuthenticationPath";

    /**
     * PATH within the web application to redirect to when a protocol error occurs. <i>[optional, default: a plain HTML page with the error
     * on it]</i>
     */
    public static final String ERROR_PAGE          = "ErrorPage";

    private static final long  serialVersionUID    = 1L;

    private static final Log   LOG                 = LogFactory.getLog(AbstractDeviceAuthenticationLandingServlet.class);

    @Init(name = AUTHENTICATION_PATH)
    protected String           authenticationPath;

    @Init(name = ERROR_PAGE, optional = true)
    protected String           errorPage;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        /*
         * Set the language cookie if language was specified in the browser post
         */
        HttpSession session = request.getSession();
        String reqLang = request.getParameter("Language");
        String reqCol = request.getParameter("Color");
        String reqMin = request.getParameter("Minimal");
        Locale language = reqLang == null || reqLang.length() == 0? null: new Locale(reqLang);
        Integer color = reqCol == null || reqCol.length() == 0? null: Integer.decode(reqCol);
        Boolean minimal = reqMin == null || reqMin.length() == 0? null: Boolean.parseBoolean(reqMin);

        if (null != language) {
            Cookie languageCookie = new Cookie(SafeOnlineCookies.LANGUAGE_COOKIE, language.getLanguage());
            languageCookie.setPath("/");
            languageCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(languageCookie);
        }
        if (null != minimal && minimal) {
            session.setAttribute(SafeOnlineAppConstants.COLOR_SESSION_ATTRIBUTE, color);
            session.setAttribute(SafeOnlineAppConstants.MINIMAL_SESSION_ATTRIBUTE, minimal);
        }

        /*
         * Start the authentication using this device.
         */
        OlasKeyStore nodeKeyStore = getKeyStore();
        try {
            Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
            handler.init(getIssuer(), nodeKeyStore.getCertificate(), nodeKeyStore.getKeyPair());
            handler.initAuthentication(request);
        } catch (AuthenticationInitializationException e) {
            ServletUtils.redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(e.getMessage()));
            return;
        }
        response.sendRedirect(authenticationPath);
    }

    /**
     * @return The issuer of the signing keys. For OLAS nodes, this is the node name.
     */
    protected abstract String getIssuer();

    /**
     * @return The signing keystore. For OLAS nodes, this is the node keystore.
     */
    protected abstract OlasKeyStore getKeyStore();
}
