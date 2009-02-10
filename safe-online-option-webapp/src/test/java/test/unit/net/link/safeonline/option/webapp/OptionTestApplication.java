package test.unit.net.link.safeonline.option.webapp;

import net.link.safeonline.option.webapp.OptionApplication;
import net.link.safeonline.wicket.test.TestStringResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OptionTestApplication extends OptionApplication {

    static final Log LOG = LogFactory.getLog(OptionTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());
    }
}
