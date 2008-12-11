/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;


@Local
public interface WSSecurityConfiguration extends SafeOnlineService, WSSecurityConfigurationService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "WSSecurityConfigurationBean/local";


    /**
     * Returns <code>true</code> if the given application wants to skip integrity at the SOAP message level. Some applications might skip
     * message level integrity check because the client side components are unable of signing the SOAP body since SSL already provides
     * transport level integrity. For example: Microsoft .NET 3.0 WCF clients.
     * 
     * @param applicationName
     * @throws ApplicationNotFoundException
     */
    boolean skipMessageIntegrityCheck(String applicationName)
            throws ApplicationNotFoundException;

}
