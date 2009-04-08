/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.password.webapp;

import net.link.safeonline.password.webapp.PasswordApplication;
import net.link.safeonline.wicket.test.TestStringResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PasswordTestApplication extends PasswordApplication {

    static final Log LOG = LogFactory.getLog(PasswordTestApplication.class);


    @Override
    protected void init() {

        super.init();

        // https://issues.apache.org/jira/browse/WICKET-2199
        getResourceSettings().getStringResourceLoaders().clear();
        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());
    }
}
