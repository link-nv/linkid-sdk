/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.metro.wsclient;

import java.net.ConnectException;


/**
 * Interface for siemens metro client.
 * 
 * @author wvdhaute
 * 
 */
public interface MetroClient {

    String getAttribute()
            throws ConnectException;
}
