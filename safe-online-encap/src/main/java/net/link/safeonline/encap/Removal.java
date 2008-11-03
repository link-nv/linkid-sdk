/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;


@Local
public interface Removal extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/RemovalBean/local";

    /*
     * Accessors.
     */
    /*
     * Actions.
     */
    String mobileRemove() throws SubjectNotFoundException, MobileException, MalformedURLException, IOException,
                         AttributeTypeNotFoundException;

    String mobileCancel() throws IOException;

    /*
     * Factories
     */
    List<AttributeDO> mobileAttributesFactory() throws SubjectNotFoundException, DeviceNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
