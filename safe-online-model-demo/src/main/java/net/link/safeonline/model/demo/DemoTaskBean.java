/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo;

import java.util.Date;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "DemoTaskBean")
public class DemoTaskBean implements Task {

    private static final Log LOG = LogFactory.getLog(DemoTaskBean.class);


    public String getName() {

        return "Human readable demo task";
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() {

        Date now = new Date();
        LOG.debug("Demo task perform: " + now);
    }
}
