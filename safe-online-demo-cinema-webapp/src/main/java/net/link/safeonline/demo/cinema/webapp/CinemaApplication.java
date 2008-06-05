package net.link.safeonline.demo.cinema.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

public class CinemaApplication extends WebApplication {

    public static String SafeOnlineAuthenticationServiceUrl;

    public static String ApplicationName;


    @Override
    protected void init() {

        getSecuritySettings().setAuthorizationStrategy(new CinemaStrategy());
        SafeOnlineAuthenticationServiceUrl = getServletContext()
                .getInitParameter("SafeOnlineAuthenticationServiceUrl");
        ApplicationName = getServletContext().getInitParameter(
                "ApplicationName");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page<?>> getHomePage() {

        return HomePage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new CinemaSession(request);
    }

}
