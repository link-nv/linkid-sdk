/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.oper;

import net.link.safeonline.webapp.Page;
import net.link.safeonline.webapp.oper.attributes.OperAttributes;
import net.link.safeonline.webapp.oper.devices.OperDevices;


public abstract class OperTemplate extends Page {

    public OperTemplate(String page) {

        super(page);
    }

    public OperApplicationsMain gotoApplicationsMain() {

        clickLinkAndWait("page_applications_link");
        return new OperApplicationsMain();
    }

    public OperAttributes gotoAttributes() {

        clickLinkAndWait("page_attributes_link");
        return new OperAttributes();
    }

    public OperConfigurationMain gotoConfiguration() {

        clickLinkAndWait("page_configuration_link");
        return new OperConfigurationMain();
    }

    public OperDevices gotoDevices() {

        clickLinkAndWait("page_devices_link");
        return new OperDevices();
    }

    public OperMaintenance gotoMaintenance() {

        clickLinkAndWait("page_maintenance_link");
        return new OperMaintenance();
    }
}
