/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.data.services;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.ClientNotFoundException;


/**
 * <p/>
 * Date: 19/03/12
 * Time: 14:39
 *
 * @author sgdesmet
 */
public interface ClientConfigurationStore {

    public ClientConfiguration getClient(String client_id)
            throws ClientNotFoundException;
}
