package net.link.safeonline.webapp.template;

import net.link.safeonline.webapp.common.ProtocolErrorPage;
import net.link.safeonline.wicket.tools.CustomStringResourceLoader;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.protocol.http.WebApplication;


public abstract class OlasApplication extends WebApplication {

    static final Log LOG = LogFactory.getLog(OlasApplication.class);


    @Override
    protected void init() {

        // Java EE annotations injector.
        WicketUtil.addInjector(this);

        mountBookmarkablePage("protocol-error", ProtocolErrorPage.class);

        getResourceSettings().addStringResourceLoader(new CustomStringResourceLoader("WEB-INF/classes/messages.webapp"));
    }
}
