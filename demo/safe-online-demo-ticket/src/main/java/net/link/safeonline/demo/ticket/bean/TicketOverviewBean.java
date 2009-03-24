/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.ticket.TicketOverview;
import net.link.safeonline.demo.ticket.entity.Ticket;
import net.link.safeonline.demo.ticket.entity.User;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;


@Stateful
@Name("ticketOverview")
@LocalBinding(jndiBinding = TicketOverview.JNDI_BINDING)
@SecurityDomain("demo-ticket")
public class TicketOverviewBean extends AbstractTicketDataClientBean implements TicketOverview {

    @Logger
    private Log            log;

    @Resource
    private SessionContext sessionContext;

    @In(value = "sessionContext")
    Context                seamSessionContext;

    @PersistenceContext(unitName = "DemoTicketEntityManager")
    private EntityManager  entityManager;

    @DataModel("ticketList")
    @SuppressWarnings("unused")
    private List<Ticket>   ticketList;


    @Factory("ticketList")
    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    public void ticketListFactory() {

        User user = entityManager.find(User.class, getUserId());
        if (null == user) {
            user = new User(getUserId(), this.getUsername());
            entityManager.persist(user);
        }
        ticketList = user.getTickets();
        log.debug("Ticket List: " + ticketList.size());
    }

    private String getUsername() {

        String username = getUsername(getUserId());
        log.debug("username #0", username);
        return username;
    }

    private String getUserId() {

        Principal principal = sessionContext.getCallerPrincipal();
        String userId = principal.getName();
        return userId;

    }
}
