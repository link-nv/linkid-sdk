/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

public class DisconnectMenuAction extends AbstractMenuAction {

    public DisconnectMenuAction() {

        super('d', "Disconnect from database");
    }

    public void run() {

        System.out.println("Disconnection from database...");
        DatabasePluginManager.disconnect();
    }

    @Override
    public boolean isActive() {

        return DatabasePluginManager.hasActiveConnection();
    }
}
