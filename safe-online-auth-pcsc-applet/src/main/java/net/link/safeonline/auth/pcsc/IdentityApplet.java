/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

public class IdentityApplet extends net.link.safeonline.identity.IdentityApplet {

    private static final long serialVersionUID = 1L;


    public IdentityApplet() {

        super(new PcscAppletController());
    }
}
