/* SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import net.link.safeonline.ctrl.ControlBaseConstants;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.entity.helpdesk.LogLevelType;
import net.link.safeonline.helpdesk.exception.HelpdeskContextNotFoundException;
import net.link.safeonline.util.ee.EjbUtils;

public class HelpdeskLogger {

	public static void clear(HttpSession session) {
		session.removeAttribute(ControlBaseConstants.HELPDESK_CONTEXT);
		List<HelpdeskEventEntity> helpdeskContext = new Vector<HelpdeskEventEntity>();
		session.setAttribute(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
	}

	@SuppressWarnings("unchecked")
	public static List<HelpdeskEventEntity> getCurrent(HttpSession session)
			throws HelpdeskContextNotFoundException {
		List<HelpdeskEventEntity> helpdeskContext = (List<HelpdeskEventEntity>) session
				.getAttribute(ControlBaseConstants.HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();
		else
			return helpdeskContext;
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
				.getAttribute(ControlBaseConstants.HELPDESK_CONTEXT);
		if (null == helpdeskContext)
			throw new HelpdeskContextNotFoundException();
		helpdeskContext.add(helpdeskEvent);
		session.setAttribute(ControlBaseConstants.HELPDESK_CONTEXT,
				helpdeskContext);
	}

	public static Long persistContext(HttpSession session)
			throws HelpdeskContextNotFoundException {

		List<HelpdeskEventEntity> helpdeskContext = getCurrent(session);

		HelpdeskManager helpdeskManager = EjbUtils.getEJB(
				"SafeOnline/HelpdeskManagerBean/local", HelpdeskManager.class);

		return helpdeskManager.persist(helpdeskContext);
	}

}
