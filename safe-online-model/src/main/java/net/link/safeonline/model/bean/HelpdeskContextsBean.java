package net.link.safeonline.model.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.dao.HelpdeskContextDAO;
import net.link.safeonline.helpdesk.dao.HelpdeskEventDAO;
import net.link.safeonline.model.HelpdeskContexts;

@Stateless
public class HelpdeskContextsBean implements HelpdeskContexts {

	@EJB
	private HelpdeskContextDAO helpdeskContextDAO;

	@EJB
	private HelpdeskEventDAO helpdeskEventDAO;

	public List<HelpdeskContextEntity> listContexts() {
		return this.helpdeskContextDAO.listContexts();
	}

	public List<HelpdeskEventEntity> listLogs(Long contextId) {
		return this.helpdeskEventDAO.listLogs(contextId);
	}

}
