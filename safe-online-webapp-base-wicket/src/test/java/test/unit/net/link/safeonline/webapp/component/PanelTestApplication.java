/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.webapp.component;

import net.link.safeonline.webapp.template.OlasApplication;
import net.link.safeonline.wicket.test.TestStringResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;


public class PanelTestApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(PanelTestApplication.class);


    @Override
    protected void init() {

        super.init();

        getResourceSettings().addStringResourceLoader(new TestStringResourceLoader());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return AttributeInputPanelPage.class;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean jaasLogin() {

        return false;
    }

}
