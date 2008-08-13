/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

public class ExitMenuAction extends AbstractMenuAction {

    public ExitMenuAction() {

        super('e', "Exit");
    }

    public void run() {

        System.out.println("Done.");
        System.exit(0);
    }
}
