/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.Stateful;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Helpdesk;
import net.link.safeonline.helpdesk.bean.HelpdeskBaseBean;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateful
@Name("helpdesk")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX + "HelpdeskBean/local")
public class HelpdeskBean extends HelpdeskBaseBean implements Helpdesk {
}
