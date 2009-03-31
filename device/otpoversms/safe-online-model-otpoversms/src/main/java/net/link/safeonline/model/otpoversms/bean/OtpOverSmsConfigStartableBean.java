/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.otpoversms.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.config.model.bean.AbstractConfigStartableBean;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = OtpOverSmsConfigStartableBean.JNDI_BINDING)
public class OtpOverSmsConfigStartableBean extends AbstractConfigStartableBean {

    public static final String JNDI_BINDING = OtpOverSmsConstants.OTPOVERSMS_STARTABLE_JNDI_PREFIX + "ConfigStartableBean";


    public OtpOverSmsConfigStartableBean() {

        configurationBeans = new Class[] { OtpOverSmsManagerBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP;
    }

}
