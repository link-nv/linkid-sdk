package net.link.safeonline.model;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;

@Local
public interface HelpdeskContexts {

	List<HelpdeskContextEntity> listContexts();

	List<HelpdeskEventEntity> listEvents(Long logId);

	// remove log ( i.e. context and events )
	void removeLog(Long logId);

	List<HelpdeskContextEntity> listUserContexts(String user);

	List<String> listUsers();

}
