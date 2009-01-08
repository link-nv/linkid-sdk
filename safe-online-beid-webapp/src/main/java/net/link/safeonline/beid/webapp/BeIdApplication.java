/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.webapp;

import net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint;
import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;


public class BeIdApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(BeIdApplication.class);


    @Override
    protected void init() {

        super.init();

        // Mount our mount points.
        for (MountPoint mountPoint : MountPoint.values()) {
            mount(mountPoint.getUCS());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return MainPage.class;
    }
}
