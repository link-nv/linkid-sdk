package net.link.safeonline.sdk.example.wicket;

import net.link.safeonline.wicket.LinkIDApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;


public class ExampleApplication extends LinkIDApplication {

    static final Log LOG = LogFactory.getLog( ExampleApplication.class );

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return MainPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new ExampleSession( request );
    }
}
