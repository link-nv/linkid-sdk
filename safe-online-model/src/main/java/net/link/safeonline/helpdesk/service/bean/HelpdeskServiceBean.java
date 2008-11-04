/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved. Lin.k N.V.
 * proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.helpdesk.service.HelpdeskService;
import net.link.safeonline.helpdesk.service.HelpdeskServiceRemote;
import net.link.safeonline.model.HelpdeskContexts;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


/**
 * Implementation of helpdesk service interface.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = HelpdeskService.JNDI_BINDING)
public class HelpdeskServiceBean implements HelpdeskService, HelpdeskServiceRemote {

    @EJB
    private HelpdeskContexts contexts;


    @RolesAllowed(SafeOnlineRoles.HELPDESK_ROLE)
    public List<HelpdeskContextEntity> listContexts() {

        return this.contexts.listContexts();

    }

    @RolesAllowed(SafeOnlineRoles.HELPDESK_ROLE)
    public List<HelpdeskEventEntity> listEvents(Long contextId) {

        return this.contexts.listEvents(contextId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed(SafeOnlineRoles.HELPDESK_ROLE)
    public void removeLog(Long logId) throws HelpdeskContextNotFoundException {

        this.contexts.removeLog(logId);
    }

    @RolesAllowed(SafeOnlineRoles.HELPDESK_ROLE)
    public List<HelpdeskContextEntity> listUserContexts(String user) {

        return this.contexts.listUserContexts(user);
    }

    @RolesAllowed(SafeOnlineRoles.HELPDESK_ROLE)
    public List<String> listUsers() {

        return this.contexts.listUsers();
    }

}
