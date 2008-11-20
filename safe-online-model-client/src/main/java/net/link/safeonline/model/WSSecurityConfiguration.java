/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;


@Local
public interface WSSecurityConfiguration extends SafeOnlineService, WSSecurityConfigurationService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "WSSecurityConfigurationBean/local";
}
