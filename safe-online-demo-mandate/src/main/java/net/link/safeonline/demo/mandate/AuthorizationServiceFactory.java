/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import net.link.safeonline.util.ee.EjbUtils;


public class AuthorizationServiceFactory {

    private AuthorizationServiceFactory() {

        // empty
    }

    public static AuthorizationService newInstance() {

        AuthorizationService authorizationService = EjbUtils.getEJB(AuthorizationService.JNDI_BINDING,
                AuthorizationService.class);
        return authorizationService;
    }
}
