package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;


public class CinemaApplication extends WebApplication {

    static final Log LOG = LogFactory.getLog(CinemaApplication.class);


    @Override
    protected void init() {

        // Java EE annotations injector.
        WicketUtil.addInjector(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return LoginPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new CinemaSession(request);

    }

}
