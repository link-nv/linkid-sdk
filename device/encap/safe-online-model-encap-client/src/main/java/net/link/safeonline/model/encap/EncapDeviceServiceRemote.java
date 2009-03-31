/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.encap;

import javax.ejb.Remote;


@Remote
public interface EncapDeviceServiceRemote extends EncapDeviceService {

    public static final String JNDI_BINDING = EncapService.JNDI_PREFIX + "EncapDeviceServiceBean/remote";

}
