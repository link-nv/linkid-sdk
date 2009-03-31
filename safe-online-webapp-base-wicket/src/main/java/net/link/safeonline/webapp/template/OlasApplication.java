package net.link.safeonline.webapp.template;

import net.link.safeonline.webapp.common.ErrorPage;
import net.link.safeonline.webapp.common.MyRequestCycle;
import net.link.safeonline.webapp.common.NotFoundPage;
import net.link.safeonline.webapp.common.ProtocolErrorPage;
import net.link.safeonline.wicket.tools.CustomStringResourceLoader;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;


public abstract class OlasApplication extends WebApplication {

    static final Log LOG = LogFactory.getLog(OlasApplication.class);


    @Override
    protected void init() {

        // Java EE annotations injector.
        WicketUtil.addInjector(this);

        mountBookmarkablePage("protocol-error", ProtocolErrorPage.class);
        mountBookmarkablePage("error", ErrorPage.class);
        mountBookmarkablePage("not-found", NotFoundPage.class);

        getResourceSettings().addStringResourceLoader(new CustomStringResourceLoader("WEB-INF/classes/messages.webapp"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {

        return new MyRequestCycle(this, (WebRequest) request, (WebResponse) response, jaasLogin());
    }

    protected abstract boolean jaasLogin();
}
