package net.link.safeonline.digipass.webapp;

import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;


public class DigipassApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(DigipassApplication.class);


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage("authentication", AuthenticationPage.class);

    }

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

        return new DigipassSession(request);

    }

}
