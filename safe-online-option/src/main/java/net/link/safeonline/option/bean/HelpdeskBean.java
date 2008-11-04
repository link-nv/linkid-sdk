/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.bean;

import javax.ejb.Stateful;

import net.link.safeonline.helpdesk.bean.HelpdeskBaseBean;
import net.link.safeonline.option.Helpdesk;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateful
@Name("optionHelpdesk")
@LocalBinding(jndiBinding = Helpdesk.JNDI_BINDING)
public class HelpdeskBean extends HelpdeskBaseBean implements Helpdesk {
}
