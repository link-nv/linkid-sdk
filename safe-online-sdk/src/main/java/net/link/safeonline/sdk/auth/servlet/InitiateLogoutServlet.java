package net.link.safeonline.sdk.auth.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.LogoutContext;
import net.link.safeonline.sdk.servlet.AbstractLinkIDInjectionServlet;


/**
 * Simple servlet to initiate logout
 *
 * User: sgdesmet
 * Date: 14/11/11
 * Time: 15:38
 */
public class InitiateLogoutServlet extends AbstractLinkIDInjectionServlet {

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
     * @see net.link.safeonline.wicket.component.linkid.LinkIDLogoutLink
     */
    public void delegate(final HttpServletRequest request, final HttpServletResponse response) {
        String targetURI = request.getParameter( "return_uri" );
        boolean redirected = false;
        if (LoginManager.isAuthenticated( request.getSession() ))
            redirected = AuthenticationUtils.logout( request, response, new LogoutContext( null, null, targetURI ) );

        if (!redirected) {
            request.getSession().invalidate();
        }
    }
}

