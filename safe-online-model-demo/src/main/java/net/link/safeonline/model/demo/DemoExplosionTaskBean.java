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
@LocalBinding(jndiBinding = Task.JNDI_PREFIX + "/" + "DemoExplosionTaskBean")
public class DemoExplosionTaskBean implements Task {

    private static final Log LOG = LogFactory.getLog(DemoExplosionTaskBean.class);


    public String getName() {

        return "Explosion demo task";
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() {

        Date now = new Date();
        LOG.debug("Demo explosion task perform: " + now);
        throw new RuntimeException("explosion");
    }
}
