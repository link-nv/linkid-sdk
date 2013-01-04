/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.servlet;

import com.lyndir.lhunath.opal.system.logging.Logger;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.util.servlet.annotation.RequestParameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;


/**
 * <h2>{@link AutoLoginServlet}<br>
 * <sub>A standard auto-login servlet.</sub></h2>
 * <p/>
 * <p>
 * Navigating to this servlet automatically creates an authentication request and redirects to link ID auth.
 * </p>
 * <p>
 * By default, the linkID.xml configuration is used for the authentication request.  You can override {@link #newAuthenticationContext(java.util.Locale, String, String)} to customize the authentication request.  You can also specify language and theme request parameters.
 * </p>
 * <p/>
 * <p>
 * <i>Jun 30, 2009</i>
 * </p>
 *
 * @author mbillemo
 */
public class AutoLoginServlet extends AbstractLinkIDInjectionServlet {

    private static final Logger logger = Logger.get( AutoLoginServlet.class );

    @RequestParameter(RequestConstants.LANGUAGE_REQUEST_PARAM)
    String languageParam;

    @RequestParameter(RequestConstants.THEME_REQUEST_PARAM)
    String themeParam;

    @RequestParameter(RequestConstants.TARGETURI_REQUEST_PARAM)
    String targetParam;

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke( request, response );
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke( request, response );
    }

    private void invoke(HttpServletRequest request, HttpServletResponse response) {

        Locale locale;
        if (null == languageParam || languageParam.isEmpty())
            locale = request.getLocale();
        else
            locale = new Locale( languageParam );

        String themeName = null;
        if (null != themeParam && !themeParam.isEmpty())
            themeName = themeParam;

        String targetURI = null;
        if (null != targetParam && !targetParam.isEmpty())
            targetURI = targetParam;

        logger.dbg( "locale.language: %s", locale.getLanguage() );
        logger.dbg( "theme: %s", themeName );

        AuthenticationUtils.login( request, response,
                newAuthenticationContext(locale, themeName, targetURI));
    }

    protected AuthenticationContext newAuthenticationContext(Locale locale, String themeName, String targetURI) {

        return new AuthenticationContext( null, null, null, false, null, null, themeName, locale, targetURI );
    }
}
