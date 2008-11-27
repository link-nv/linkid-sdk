package test.unit.net.link.safeonline.digipass.webapp;

import net.link.safeonline.demo.wicket.test.TestStringResourceLoader;
import net.link.safeonline.digipass.webapp.DigipassApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DigipassTestApplication extends DigipassApplication {

    static final Log LOG = LogFactory.getLog(DigipassTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());

    }
}
