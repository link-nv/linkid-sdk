/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.servlet;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.ForceAuth;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AuthorizationRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.MessageUtils;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.safeonline.sdk.servlet.AbstractLinkIDInjectionServlet;
import org.jetbrains.annotations.Nullable;


/**
 * Servlet to link mobile applications to your application. The underlying protocol is based on OAuth (only OAuth is supported)
 * Works together with {@code MobileLandingServlet}
 * <p/>
 * Date: 15/05/12
 * Time: 15:27
 *
 * @author sgdesmet
 */
public class MobileLinkingServlet extends AbstractLinkIDInjectionServlet {

    private static final Logger logger = Logger.get( MobileLinkingServlet.class );

    public static final String MOBILE_CLIENTID_PARAM = "clientId";
    public static final String LANDING_URL_PARAM     = "LandingURL";
    public static final String MODE_PARAM            = "LoginMode";

    private String clientId;

    /**
     * Path of {@code MobileLandingServlet}
     */
    private String landingURL;
    private String loginMode;

    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init( config );

        clientId = config.getInitParameter( MOBILE_CLIENTID_PARAM );
        landingURL = config.getInitParameter( LANDING_URL_PARAM );
        loginMode = config.getInitParameter( MODE_PARAM );
        if (null == loginMode)
            loginMode = "FRAMED";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        delegate( response );
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        delegate( response );
    }

    /**
     * create oauth request and send
     */
    public void delegate(final HttpServletResponse response) {

        logger.dbg( "mobile linking request" );

        //optional target URL: when login is complete, user will be redirected to this location
        if (clientId == null || clientId.length() == 0)
            throw new InternalInconsistencyException( "AppName parameter must be set" );

        if (landingURL == null || landingURL.length() == 0)
            throw new InternalInconsistencyException( "LandingURL parameter must be set" );

        try {
            sendOAuthRequestMessage( response, null );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( e );
        }
    }

    private void sendOAuthRequestMessage(HttpServletResponse response, @Nullable String state)
            throws IOException {

        String authnService = ConfigUtils.getLinkIDAuthURLFromPath( config().proto().oauth2().authorizationPath() );

        // create oauht2 authorization request ( authorization grant code flow)
        AuthorizationRequest authorizationRequest = new AuthorizationRequest( OAuth2Message.ResponseType.CODE, clientId );
        authorizationRequest.setRedirectUri( ConfigUtils.getApplicationConfidentialURLFromPath( landingURL ) );
        authorizationRequest.setState( state );
        // note: scope is not set in the authorization request, this is preconfigured by the linkid operator
        boolean paramsInBody = config().proto().oauth2().binding().contains( "POST" );
        // add login mode (this is not part of oauth, but linkid)
        List<String> loginParams = new ArrayList<String>( 2 );
        loginParams.add( RequestConstants.LOGINMODE_REQUEST_PARAM );
        loginParams.add( loginMode );
        loginParams.add( RequestConstants.OAUTH2_FORCE_AUTHN );
        loginParams.add( ForceAuth.AUTO.toString() ); //use SSO if possible

        MessageUtils.sendRedirectMessage( authnService, authorizationRequest, response, paramsInBody, loginParams );
    }
}
