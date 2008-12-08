/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.digipass;

import javax.ejb.Remote;


@Remote
public interface DigipassDeviceServiceRemote extends DigipassDeviceService {

    public static final String JNDI_BINDING = DigipassService.JNDI_PREFIX + "DigipassDeviceServiceBean/remote";

}
