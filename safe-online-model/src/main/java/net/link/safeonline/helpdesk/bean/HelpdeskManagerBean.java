/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class HelpdeskManagerBean implements HelpdeskManager {

    private final static Log   LOG = LogFactory.getLog(HelpdeskManagerBean.class);

    @EJB
    private HelpdeskContextDAO helpdeskContextDAO;

    @EJB
    private HelpdeskEventDAO   helpdeskEventDAO;


    public Long persist(String location, List<HelpdeskEventEntity> helpdeskEventList) {

        HelpdeskContextEntity context = this.helpdeskContextDAO.createHelpdeskContext(location);

        LOG.debug("persist helpdeskcontext ( id=" + context.getId() + " )");

        for (HelpdeskEventEntity helpdeskEvent : helpdeskEventList) {
            helpdeskEvent.setHelpdeskContext(context);

        }
        this.helpdeskEventDAO.persist(helpdeskEventList);
        return context.getId();

    }

}
