/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;


@Local
public interface AttributeAdd extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AttributeAddBean/local";

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Actions.
     */
    String commit() throws AttributeTypeNotFoundException;

    /*
     * Factories.
     */
    void attributeEditContextFactory() throws AttributeTypeNotFoundException;
}
