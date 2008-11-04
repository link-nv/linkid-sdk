package net.link.safeonline.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.model.HelpdeskContexts;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = HelpdeskContexts.JNDI_BINDING)
public class HelpdeskContextsBean implements HelpdeskContexts {

    @EJB
    private HelpdeskContextDAO helpdeskContextDAO;

    @EJB
    private HelpdeskEventDAO   helpdeskEventDAO;


    public List<HelpdeskContextEntity> listContexts() {

        return this.helpdeskContextDAO.listContexts();
    }

    public List<HelpdeskEventEntity> listEvents(Long contextId) {

        return this.helpdeskEventDAO.listEvents(contextId);
    }

    public void removeLog(Long logId) throws HelpdeskContextNotFoundException {

        this.helpdeskEventDAO.removeEvents(logId);
        this.helpdeskContextDAO.removeContext(logId);
    }

    public List<HelpdeskContextEntity> listUserContexts(String user) {

        return this.helpdeskEventDAO.listUserContexts(user);
    }

    public List<String> listUsers() {

        return this.helpdeskEventDAO.listUsers();
    }

}
