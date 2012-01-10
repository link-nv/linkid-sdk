package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.RequestConstants;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.*;
import net.link.safeonline.sdk.servlet.AbstractLinkIDInjectionServlet;


/**
 * A simple Servlet to initiate the login procedure on LinkID (i.e. this servlet represents a 'protected resource' which requires auhtenication).
 * Landing on this servlet will return a redirect url to LinkID authentication service, an authentication Request, and possibly additional parameters.
 *
 * User: sgdesmet
 * Date: 03/11/11
 * Time: 10:21
 */
public class InitiateLoginServlet extends AbstractLinkIDInjectionServlet {

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        delegate( request, response );
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        delegate( request, response );
    }

    /**
     * create authentication context and start login
     * @param request
     * @param response
     */
    public void delegate(final HttpServletRequest request, final HttpServletResponse response) {
        //optional target URL: when login is complete, user will be redirected to this location
        String targetURI = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );
        String modeParam = request.getParameter( RequestConstants.LOGINMODE_REQUEST_PARAM );
        LoginMode mode = null;
        if (modeParam != null){
            for (LoginMode val : LoginMode.values()){
                if (modeParam.trim().equalsIgnoreCase( val.name() )){
                    mode = val;
                    break;
                }
            }
        }
            AuthenticationUtils.login( request, response,  new AuthenticationContext(null, null, null, targetURI, mode) );
    }

}
