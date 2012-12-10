package net.link.safeonline.sdk.example.wicket;

import net.link.safeonline.wicket.LinkIDApplication;
import net.link.util.j2ee.NamingStrategy;
import org.apache.wicket.*;


public class ExampleApplication extends LinkIDApplication {

    @Override
    public Class<? extends Page> getHomePage() {

        return MainPage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {

        return new ExampleSession( request );
    }

    @Override
    protected NamingStrategy findNamingStrategy() {

        return null;
    }
}
