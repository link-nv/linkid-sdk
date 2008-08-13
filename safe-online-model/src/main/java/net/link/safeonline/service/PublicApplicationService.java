/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import javax.ejb.Local;

import net.link.safeonline.model.application.PublicApplication;


/**
 * Interface to service for retrieving public information about applications.
 * 
 * @author mbillemo
 * 
 */
@Local
public interface PublicApplicationService {

    public final String JNDI_BINDING = "SafeOnline/PublicApplicationServiceBean/local";


    /**
     * Gives back a stub for an application that contains the application's data that's available to the public.
     * 
     * @param applicationName
     */
    public PublicApplication findPublicApplication(String applicationName);
}
