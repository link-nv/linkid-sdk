package net.link.safeonline.ctrl.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.ctrl.Help;
import net.link.safeonline.helpdesk.Helpdesk;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;

@Stateful
@Name("help")
public class HelpBean implements Help {

	@Logger
	private Log log;
	@In
	private Helpdesk helpdeskManager;

	@In
	Context sessionContext;

	@Out
	Long id;

	public String log() {
		log.debug("persisting volatile log");
		id = helpdeskManager.persistContext();
		return "logged";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}

}
