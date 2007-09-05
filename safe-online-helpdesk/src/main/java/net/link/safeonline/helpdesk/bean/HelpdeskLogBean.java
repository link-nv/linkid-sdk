/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.entity.helpdesk.HelpdeskContextEntity;
import net.link.safeonline.entity.helpdesk.HelpdeskEventEntity;
import net.link.safeonline.helpdesk.HelpdeskConstants;
import net.link.safeonline.helpdesk.HelpdeskLog;
import net.link.safeonline.helpdesk.service.HelpdeskService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("helpdeskLog")
@LocalBinding(jndiBinding = HelpdeskConstants.JNDI_PREFIX
		+ "HelpdeskLogBean/local")
@SecurityDomain(HelpdeskConstants.SAFE_ONLINE_HELPDESK_SECURITY_DOMAIN)
public class HelpdeskLogBean implements HelpdeskLog {

	private static final Log LOG = LogFactory.getLog(HelpdeskLogBean.class);

	@EJB
	private HelpdeskService helpdeskService;

	private final static String HELPDESK_CONTEXT_LIST_NAME = "helpdeskContextList";

	private final static String HELPDESK_LOG_LIST_NAME = "helpdeskLogList";

	@DataModel(HELPDESK_CONTEXT_LIST_NAME)
	private List<HelpdeskContextEntity> helpdeskContextList;

	@DataModelSelection(HELPDESK_CONTEXT_LIST_NAME)
	@Out(value = "selectedContext", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private HelpdeskContextEntity selectedContext;

	@DataModel(HELPDESK_LOG_LIST_NAME)
	private List<HelpdeskEventEntity> helpdeskLogList;

	@DataModelSelection(HELPDESK_LOG_LIST_NAME)
	@Out(value = "selectedLog", required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private HelpdeskEventEntity selectedLog;

	@In(create = true)
	FacesMessages facesMessages;

	@Factory(HELPDESK_CONTEXT_LIST_NAME)
	@RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
	public void helpdeskContextListFactory() {
		LOG.debug("helpdesk context list factory");
		this.helpdeskContextList = this.helpdeskService.listContexts();
	}

	@Factory(HELPDESK_LOG_LIST_NAME)
	@RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
	public void helpdeskLogListFactory() {
		LOG.debug("helpdesk log list factory ( id=" + selectedContext.getId()
				+ " )");
		this.helpdeskLogList = this.helpdeskService.listLogs(selectedContext
				.getId());
	}

	@RolesAllowed(HelpdeskConstants.HELPDESK_ROLE)
	public String view() {
		LOG.debug("view log: " + this.selectedContext.getId());
		return "view";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		this.selectedContext = null;
	}
}
