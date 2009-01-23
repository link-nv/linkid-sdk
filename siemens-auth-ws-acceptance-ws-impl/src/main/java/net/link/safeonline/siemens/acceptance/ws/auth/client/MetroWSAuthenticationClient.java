/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.client;

import java.net.ConnectException;


/**
 * Interface for siemens metro client.
 * 
 * @author wvdhaute
 * 
 */
public interface MetroWSAuthenticationClient {

    String getAttribute()
            throws ConnectException;
}
