/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.servlet;


import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.device.sdk.auth.saml2.Saml2Handler;
import net.link.safeonline.device.sdk.exception.AuthenticationInitializationException;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Init;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;


public abstract class LandingServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(LandingServlet.class);

    @Init(name = "AuthenticationUrl")
    protected String          authenticationUrl;

    @Init(name = "ServletEndpointUrl", optional = true)
    protected String          servletEndpointUrl;

    @Init(name = "ErrorPage", optional = true)
    protected String          errorPage;

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        /**
         * Wrap the request to use the servlet endpoint url if defined. To prevent failure when behind a reverse proxy or loadbalancer when
         * opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper;
        if (null != servletEndpointUrl) {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, servletEndpointUrl);
        } else {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, request.getRequestURL().toString());
        }

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
            session.setAttribute(SafeOnlineAppConstants.COLOR_ATTRIBUTE, color);
            session.setAttribute(SafeOnlineAppConstants.MINIMAL_ATTRIBUTE, minimal);
        }

        /*
         * Start the authentication using this device.
         */
        OlasKeyStore olasKeyStore = getOlasKeyStore();
        try {
            Saml2Handler handler = Saml2Handler.getSaml2Handler(requestWrapper);
            handler.init(configParams, olasKeyStore.getCertificate(), olasKeyStore.getKeyPair());
            handler.initAuthentication(requestWrapper);
        } catch (AuthenticationInitializationException e) {
            redirectToErrorPage(requestWrapper, response, errorPage, null, new ErrorMessage(e.getMessage()));
            return;
        }
        response.sendRedirect(authenticationUrl);
    }

    protected abstract OlasKeyStore getOlasKeyStore();
}
