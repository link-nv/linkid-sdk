/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.application;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;


/**
 * Factory for domain model Application objects.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationFactory {

    private ApplicationFactory() {

        // empty
    }

    /**
     * Gives back the domain model object corresponding with the given application name.
     * 
     * @param applicationContext
     * @param applicationName
     * @throws ApplicationNotFoundException
     */
    public static Application getApplication(ApplicationContext applicationContext, String applicationName)
            throws ApplicationNotFoundException {

        ApplicationEntity applicationEntity = applicationContext.getApplicationDAO().getApplication(applicationName);
        Application application = new Application(applicationEntity);
        return application;
    }
}
