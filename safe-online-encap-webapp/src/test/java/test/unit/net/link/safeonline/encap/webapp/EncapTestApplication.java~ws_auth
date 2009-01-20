package test.unit.net.link.safeonline.encap.webapp;

import net.link.safeonline.encap.webapp.EncapApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EncapTestApplication extends EncapApplication {

    static final Log LOG = LogFactory.getLog(EncapTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());
    }
}
