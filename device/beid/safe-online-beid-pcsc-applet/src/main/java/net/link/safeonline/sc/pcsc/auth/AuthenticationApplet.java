/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.auth;

import net.link.safeonline.sc.pcsc.common.PcscAppletController;


public class AuthenticationApplet extends net.link.safeonline.sc.pkcs11.auth.AuthenticationApplet {

    private static final long serialVersionUID = 1L;


    public AuthenticationApplet() {

        super(new PcscAppletController());
    }
}
