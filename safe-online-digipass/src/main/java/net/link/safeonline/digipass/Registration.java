/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface Registration extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/RegistrationBean/local";

    /*
     * Accessors.
     */
    String getLoginName();

    void setLoginName(String loginName);

    String getSerialNumber();

    void setSerialNumber(String serialNumber);

    /*
     * Actions.
     */
    String register() throws PermissionDeniedException, SubjectNotFoundException, ArgumentIntegrityException,
                     AttributeTypeNotFoundException;

    /*
     * Factories
     */

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
