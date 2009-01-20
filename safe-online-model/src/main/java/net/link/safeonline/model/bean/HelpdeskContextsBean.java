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

    @EJB(mappedName = HelpdeskContextDAO.JNDI_BINDING)
    private HelpdeskContextDAO helpdeskContextDAO;

    @EJB(mappedName = HelpdeskEventDAO.JNDI_BINDING)
    private HelpdeskEventDAO   helpdeskEventDAO;


    public List<HelpdeskContextEntity> listContexts() {

        return helpdeskContextDAO.listContexts();
    }

    public List<HelpdeskEventEntity> listEvents(Long contextId) {

        return helpdeskEventDAO.listEvents(contextId);
    }

    public void removeLog(Long logId)
            throws HelpdeskContextNotFoundException {

        helpdeskEventDAO.removeEvents(logId);
        helpdeskContextDAO.removeContext(logId);
    }

    public List<HelpdeskContextEntity> listUserContexts(String user) {

        return helpdeskEventDAO.listUserContexts(user);
    }

    public List<String> listUsers() {

        return helpdeskEventDAO.listUsers();
    }

}
