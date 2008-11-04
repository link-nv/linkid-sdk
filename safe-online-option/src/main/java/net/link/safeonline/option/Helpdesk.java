/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option;

import javax.ejb.Local;

import net.link.safeonline.option.OptionConstants;
import net.link.safeonline.helpdesk.HelpdeskBase;


@Local
public interface Helpdesk extends HelpdeskBase {

    public static final String JNDI_BINDING = OptionConstants.JNDI_PREFIX + "HelpdeskBean/local";
}
