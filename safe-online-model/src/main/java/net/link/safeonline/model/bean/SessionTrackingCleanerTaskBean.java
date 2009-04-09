/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.Task;
import net.link.safeonline.dao.SessionTrackingDAO;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@Local(Task.class)
@LocalBinding(jndiBinding = SessionTrackingCleanerTaskBean.JNDI_BINDING)
public class SessionTrackingCleanerTaskBean implements Task {

    public static final String  JNDI_BINDING = Task.JNDI_PREFIX + "SessionTrackingCleanerTaskBean/local";

    private static final String name         = "Session tracking cleaner";

    @EJB(mappedName = SessionTrackingDAO.JNDI_BINDING)
    private SessionTrackingDAO  sessionTrackingDAO;


    public SessionTrackingCleanerTaskBean() {

        // empty
    }

    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        sessionTrackingDAO.clearExpired();
    }

}
