/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.dao.AttributeCacheDAO;
import net.link.safeonline.entity.AttributeCacheEntity;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = AttributeCacheCleanerTaskBean.JNDI_BINDING)
public class AttributeCacheCleanerTaskBean implements Task {

    private static final String name = "Attribute cache cleaner";

    public static final String JNDI_BINDING = Task.JNDI_PREFIX + "/AttributeCacheCleanerTaskBean/local";

    @EJB
    private AttributeCacheDAO   attributeCacheDAO;


    public AttributeCacheCleanerTaskBean() {

        // empty
    }

    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform() throws Exception {

        long currentTime = System.currentTimeMillis();
        List<AttributeCacheEntity> attributes = this.attributeCacheDAO.listAttributes();
        for (AttributeCacheEntity attribute : attributes) {
            if (currentTime - attribute.getEntryDate().getTime() > attribute.getAttributeType().getAttributeCacheTimeoutMillis()) {
                // expired
                this.attributeCacheDAO.removeAttribute(attribute);
            }

        }
    }

}
