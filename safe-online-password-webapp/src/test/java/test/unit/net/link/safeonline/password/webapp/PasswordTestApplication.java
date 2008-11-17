/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.password.webapp;

import net.link.safeonline.demo.wicket.test.TestStringResourceLoader;
import net.link.safeonline.password.webapp.PasswordApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PasswordTestApplication extends PasswordApplication {

    static final Log LOG = LogFactory.getLog(PasswordTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());

    }
}
