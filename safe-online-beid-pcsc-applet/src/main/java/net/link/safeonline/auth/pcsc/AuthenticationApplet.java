/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

public class AuthenticationApplet extends net.link.safeonline.auth.AuthenticationApplet {

    private static final long serialVersionUID = 1L;


    public AuthenticationApplet() {

        super(new PcscAppletController());
    }
}
