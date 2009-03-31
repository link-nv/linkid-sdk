/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password;

import javax.ejb.Remote;


@Remote
public interface PasswordDeviceServiceRemote extends PasswordDeviceService {

    public static final String JNDI_BINDING = PasswordService.JNDI_PREFIX + "PasswordDeviceServiceBean/remote";

}
