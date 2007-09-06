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
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.servlet.http.HttpSession;

import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;
import net.link.safeonline.helpdesk.Helpdesk;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Context;

@Stateless
@Name("helpdesk")
public class HelpdeskBean implements Helpdesk {

	private final static Log LOG = LogFactory.getLog(HelpdeskBean.class);

	public final static String HELPDESK_CONTEXT = "helpdeskContext";

	@In
	Context sessionContext;

	@Resource
	private SessionContext context;

	@EJB
	private HelpdeskManager helpdeskManager;

	public void clear() {
		this.sessionContext.remove(HELPDESK_CONTEXT);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		this.sessionContext.set(HELPDESK_CONTEXT, helpdeskContext);
	}

	public static void clear(HttpSession session) {
		session.removeAttribute(HELPDESK_CONTEXT);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		session.setAttribute(HELPDESK_CONTEXT, helpdeskContext);
	}

	@SuppressWarnings("unchecked")
	public List<HelpdeskEventEntity> getCurrent()
			throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) this.sessionContext
				.get(HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();
		else
			return helpdeskContext;
	}

	@SuppressWarnings("unchecked")
	public static List<HelpdeskEventEntity> getCurrent(HttpSession session)
			throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) session
				.getAttribute(HELPDESK_CONTEXT);
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
				.get(HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();

		helpdeskContext.add(helpdeskEvent);
		this.sessionContext.set(HELPDESK_CONTEXT, helpdeskContext);
	}

	@SuppressWarnings("unchecked")
	public static void add(HttpSession session, String message,
			LogLevelType logLevel) throws HelpdeskContextNotFoundException {
		String principal = (String) session.getAttribute("username");
		if (null == principal) {
			principal = "unknown_user";
		}

		HelpdeskEventEntity helpdeskEvent = new HelpdeskEventEntity(message,
				principal, logLevel);
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) session
				.getAttribute(HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();
		helpdeskContext.add(helpdeskEvent);
		session.setAttribute(HELPDESK_CONTEXT, helpdeskContext);
	}

	public Long persistContext() throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = getCurrent();
		return helpdeskManager.persist(helpdeskContext);
	}

	public static Long persistContext(HttpSession session)
			throws HelpdeskContextNotFoundException {

		List<HelpdeskEventEntity> helpdeskContext = getCurrent(session);

		LOG.debug("lookup HelpdeskManager bean");
		HelpdeskManager helpdeskManager = EjbUtils.getEJB(
				"SafeOnline/HelpdeskManagerBean/local", HelpdeskManager.class);

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
			LOG.debug("getCallerPrincipal throws IllegalStateException");
			return null;
		}
		if (null == callerPrincipal) {
			return null;
		}
		String name = callerPrincipal.getName();
		return name;
	}

}