/*
 * SafeOnline project.
 *
 * Copyright 2005-2006 Frank Cornelis.
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.io.IOException;


/**
 * Signals that no smart card was found in the smart card reader.
 *
 * @author fcorneli
 *
 */
public class SmartCardNotFoundException extends IOException {

    private static final long serialVersionUID = 1L;


    public SmartCardNotFoundException() {

        super();
    }

    public SmartCardNotFoundException(String s) {

        super(s);
    }
}
