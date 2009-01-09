/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.service.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.demo.ticket.entity.Ticket;
import net.link.safeonline.demo.ticket.entity.User;
import net.link.safeonline.demo.ticket.entity.Ticket.Site;
import net.link.safeonline.demo.ticket.service.TicketService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.joda.time.DateTime;


@Stateless
@LocalBinding(jndiBinding = TicketService.JNDI_BINDING)
public class TicketServiceBean implements TicketService {

    private static final Log LOG = LogFactory.getLog(TicketServiceBean.class);

    @PersistenceContext(unitName = "DemoTicketEntityManager")
    private EntityManager    entityManager;


    @SuppressWarnings("unchecked")
    public boolean hasValidPass(String nrn, String from, String to) {

        LOG.debug("has valid pass: " + nrn + " from " + from + " to " + to);

        Query userQuery = User.createQueryWhereNrn(entityManager, nrn);
        List<User> users = userQuery.getResultList();
        if (users.isEmpty()) {
            LOG.debug("no matching user found for NRN: " + nrn);
            return false;
        }

        User user = users.get(0);
        LOG.debug("user located: " + user.getSafeOnlineUserName());

        Site start;
        Site destination;
        try {
            start = Site.valueOf(from);
            destination = Site.valueOf(to);
        } catch (IllegalArgumentException e) {
            LOG.debug("illegal argument: " + e.getMessage());
            return false;
        }
        Query ticketQuery = Ticket.createQueryWhereOwner(entityManager, user);
        List<Ticket> tickets = ticketQuery.getResultList();
        if (tickets.isEmpty()) {
            LOG.debug("no passes found for user: " + user.getSafeOnlineUserName());
            return false;
        }

        for (Ticket ticket : tickets) {
            if (ticket.isBiDirectional()) {
                if (start != ticket.getStart() && destination != ticket.getStart()) {
                    continue;
                }
                if (destination != ticket.getDestination() && start != ticket.getDestination()) {
                    continue;
                }
            } else {
                if (start != ticket.getStart()) {
                    continue;
                }
                if (destination != ticket.getDestination()) {
                    continue;
                }
            }
            DateTime beginDate = new DateTime(ticket.getValidFrom());
            DateTime endDate = new DateTime(ticket.getValidTo());
            if (beginDate.isAfterNow()) {
                continue;
            }
            if (endDate.isBeforeNow()) {
                continue;
            }
            return true;
        }

        return false;
    }
}
