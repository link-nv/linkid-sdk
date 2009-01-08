package test.unit.net.link.safeonline.beid.webapp;

import net.link.safeonline.beid.webapp.BeIdApplication;
import net.link.safeonline.wicket.test.TestStringResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BeIdTestApplication extends EncapApplication {

    static final Log LOG = LogFactory.getLog(BeIdTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());
    }
}
