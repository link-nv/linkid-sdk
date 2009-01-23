/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.auth.ws.acceptance.jaxws.ws.client;

import java.net.ConnectException;


public interface SiemensAuthWsAcceptanceClient {

    String getAttribute()
            throws ConnectException;
}
