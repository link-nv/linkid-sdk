/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import net.link.safeonline.auth.AuthenticationApplet;


/**
 * Applet for quick identification of a user via the BeID card using the PC/SC interface.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationApplet extends AuthenticationApplet {

    private static final long serialVersionUID = 1L;


    public IdentificationApplet() {

        super(new IdentificationController());
    }
}
