/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.user.UserConstants;
import net.link.safeonline.ctrl.HistoryMessage;


@Local
public interface History {

    public static final String JNDI_BINDING = UserConstants.JNDI_PREFIX + "HistoryBean/local";

    List<HistoryMessage> getList();
}
