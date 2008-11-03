/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.ctrl.LoginBase;


@Local
public interface Login extends SafeOnlineService, LoginBase {
}
