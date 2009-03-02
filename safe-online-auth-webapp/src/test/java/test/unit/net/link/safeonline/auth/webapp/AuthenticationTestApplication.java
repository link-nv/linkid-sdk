/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.webapp;

import net.link.safeonline.auth.webapp.AuthenticationApplication;
import net.link.safeonline.wicket.test.TestStringResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AuthenticationTestApplication extends AuthenticationApplication {

    static final Log LOG = LogFactory.getLog(AuthenticationTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());

    }
}
