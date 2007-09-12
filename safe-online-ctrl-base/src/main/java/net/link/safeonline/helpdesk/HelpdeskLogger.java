/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.ctrl.ControlBaseConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HelpdeskLogger {

	private static final Log LOG = LogFactory.getLog(HelpdeskLogger.class);

	/*
	 * Clear the volatile helpdesk context on the HttpSession
	 */
	public static List<HelpdeskEventEntity> clear() {
		return clear(getHttpSession());
	}

	public static List<HelpdeskEventEntity> clear(HttpSession session) {
		session.removeAttribute(ControlBaseConstants.HELPDESK_CONTEXT);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		session.setAttribute(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
		LOG.debug("new volatlie helpdesk context created");
		return helpdeskContext;
	}

	/*
	 * Get the volatile context from the HttpSession
	 */
	public static List<HelpdeskEventEntity> getCurrent() {
		return getCurrent(getHttpSession());
	}

	@SuppressWarnings("unchecked")
	public static List<HelpdeskEventEntity> getCurrent(HttpSession session) {
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) session
				.getAttribute(ControlBaseConstants.HELPDESK_CONTEXT);
		if (null == helpdeskContext) {
			helpdeskContext = new Vector<HelpdeskEventEntity>();
			session.setAttribute(ControlBaseConstants.HELPDESK_CONTEXT,
					helpdeskContext);
		}
		return helpdeskContext;
	}

	/*
	 * Add a helpdesk event to the volatile context
	 */
	public static void add(String message, LogLevelType logLevel) {
		add(getHttpSession(), message, logLevel);
	}

	public static void add(HttpSession session, String message,
			LogLevelType logLevel) {
		String principal = getPrincipal(session);
		add(session, message, principal, logLevel);
	}

	private static String getPrincipal(HttpSession session) {
		String principal = (String) session.getAttribute("username");
		if (null == principal) {
			principal = "unknown";
		}
		return principal;
	}

	@SuppressWarnings("unchecked")
	private static void add(HttpSession session, String message,
			String principal, LogLevelType logLevel) {
		HelpdeskEventEntity helpdeskEvent = new HelpdeskEventEntity(message,
				principal, logLevel);
		List<HelpdeskEventEntity> helpdeskContext = getCurrent(session);
		helpdeskContext.add(helpdeskEvent);
		session.setAttribute(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
		LOG.debug("add helpdesk event ( context-size=" + helpdeskContext.size()
				+ " ) : " + message);
	}

	/*
	 * Persist the volatile helpdesk context
	 */
	public static Long persistContext() {
		return persistContext(getLocation(), getHttpSession());
	}

	public static Long persistContext(String location, HttpSession session) {
		List<HelpdeskEventEntity> helpdeskContext = getCurrent(session);

		/*
		 * add extra information
		 */
		Enumeration<?> initParameterNames = session.getServletContext()
				.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			String name = (String) initParameterNames.nextElement();
			String value = session.getServletContext().getInitParameter(name);
			add(session, "Servlet context: " + name + "=" + value,
					getPrincipal(session), LogLevelType.INFO);
		}

		add(session, "Server info: "
				+ session.getServletContext().getServerInfo(),
				LogLevelType.INFO);
		add(session, "Servlet context path: "
				+ session.getServletContext().getServletContextName(),
				LogLevelType.INFO);

		HelpdeskManager helpdeskManager = EjbUtils.getEJB(
				"SafeOnline/HelpdeskManagerBean/local", HelpdeskManager.class);

		LOG.debug("persisting volatile context...");
		return helpdeskManager.persist(location, helpdeskContext);
	}

	/*
	 * return HttpSession via FacesContext
	 */
	private static HttpSession getHttpSession() {
		return (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
	}

	private static String getLocation() {
		return (String) FacesContext.getCurrentInstance().getExternalContext()
				.getRequestContextPath()
				+ (String) FacesContext.getCurrentInstance()
						.getExternalContext().getRequestServletPath();

	}
}
