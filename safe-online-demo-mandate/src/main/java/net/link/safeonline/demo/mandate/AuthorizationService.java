/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import javax.ejb.Local;


@Local
public interface AuthorizationService {

    static final String JNDI_BINDING       = "SafeOnlineMandateDemo/AuthorizationServiceBean/local";

    static final String DEFAULT_ADMIN_USER = "mandate-admin";


    boolean isAdmin(String userId);

    void bootstrap();
}
