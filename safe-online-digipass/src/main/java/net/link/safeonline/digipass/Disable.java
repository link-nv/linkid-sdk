/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.DeviceRegistrationDO;


@Local
public interface Disable extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/DisableBean/local";

    /*
     * Accessors.
     */
    /*
     * Actions.
     */
    String save() throws IOException, SubjectNotFoundException, AttributeNotFoundException;

    String cancel() throws IOException;

    /*
     * Factories
     */
    List<DeviceRegistrationDO> registrationsFactory() throws SubjectNotFoundException, DeviceNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
