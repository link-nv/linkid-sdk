/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid;

import javax.ejb.Local;

import net.link.safeonline.helpdesk.HelpdeskBase;
import net.link.safeonline.model.beid.BeIdService;


@Local
public interface Helpdesk extends HelpdeskBase {

    public static final String JNDI_BINDING = BeIdService.JNDI_PREFIX + "HelpdeskBean/local";
}
