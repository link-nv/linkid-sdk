/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import javax.ejb.Remote;


@Remote
public interface OtpServiceRemote extends OtpService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpServiceBean/remote";

}
