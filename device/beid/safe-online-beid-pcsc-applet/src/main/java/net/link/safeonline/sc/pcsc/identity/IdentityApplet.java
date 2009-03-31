/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.identity;

import net.link.safeonline.sc.pcsc.common.PcscAppletController;


public class IdentityApplet extends net.link.safeonline.sc.pkcs11.identity.IdentityApplet {

    private static final long serialVersionUID = 1L;


    public IdentityApplet() {

        super(new PcscAppletController());
    }
}
