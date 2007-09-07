/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import java.security.Principal;
import java.util.List;
import java.util.Vector;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.SessionContext;

import net.link.safeonline.ctrl.ControlBaseConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;
import net.link.safeonline.helpdesk.HelpdeskBase;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.log.Log;

public class HelpdeskBaseBean implements HelpdeskBase {

	@Logger
	private Log log;

	@In
	Context sessionContext;

	@Out
	Long id;

	@Resource
	private SessionContext context;

	@EJB
	private HelpdeskManager helpdeskManager;

	public String log() {
		log.debug("persisting volatile log");
		id = persistContext();
		return "logged";
	}

	public void clear() {
		this.sessionContext.remove(ControlBaseConstants.HELPDESK_CONTEXT);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		this.sessionContext.set(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
	}

	@SuppressWarnings("unchecked")
	public List<HelpdeskEventEntity> getCurrent()
			throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) this.sessionContext
				.get(ControlBaseConstants.HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();
		else
			return helpdeskContext;
	}

	@SuppressWarnings("unchecked")
	public void add(String message, LogLevelType logLevel)
			throws HelpdeskContextNotFoundException {

		String principalName = getCallerPrincipalName();

		HelpdeskEventEntity helpdeskEvent = new HelpdeskEventEntity(message,
				principalName, logLevel);
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) this.sessionContext
				.get(ControlBaseConstants.HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();

		helpdeskContext.add(helpdeskEvent);
		this.sessionContext.set(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
	}

	public Long persistContext() throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = getCurrent();
		return helpdeskManager.persist(helpdeskContext);
	}

	private String getCallerPrincipalName() {
		Principal callerPrincipal;
		try {
			callerPrincipal = this.context.getCallerPrincipal();
		} catch (IllegalStateException e) {
			/*
			 * Under JBoss we get an IllegalStateException instead of a null
			 * principal if there is no identifiable caller principal.
			 */
			log.debug("getCallerPrincipal throws IllegalStateException");
			return null;
		}
		if (null == callerPrincipal) {
			return null;
		}
		String name = callerPrincipal.getName();
		return name;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}

}