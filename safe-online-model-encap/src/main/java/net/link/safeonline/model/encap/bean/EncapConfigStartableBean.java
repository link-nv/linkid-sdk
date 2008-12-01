/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.config.model.bean.AbstractConfigStartableBean;
import net.link.safeonline.model.encap.EncapConstants;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = EncapConfigStartableBean.JNDI_BINDING)
public class EncapConfigStartableBean extends AbstractConfigStartableBean {

    public static final String JNDI_BINDING = EncapConstants.ENCAP_STARTABLE_JNDI_PREFIX + "ConfigStartableBean";


    public EncapConfigStartableBean() {

        this.configurationBeans = new Class[] { MobileManagerBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP;
    }

}
