/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.config.ConfigItemEntity;


/**
 * Configuration Manager interface. Allows for components to access the configuration outside of a security domain. Also manages some
 * configuration parameters that do not belong to a particular model bean.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ConfigurationManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/ConfigurationManagerBean/local";


    ConfigItemEntity findConfigItem(String name);

    long getMaximumWsSecurityTimestampOffset();

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
