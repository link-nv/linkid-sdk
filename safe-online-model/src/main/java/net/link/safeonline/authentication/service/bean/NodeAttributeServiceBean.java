/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineNodeRoles;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAttributeService;
import net.link.safeonline.authentication.service.NodeAttributeServiceRemote;
import net.link.safeonline.authentication.service.ProxyAttributeService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Attribute Service Implementation for nodes.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_NODE_SECURITY_DOMAIN)
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class })
public class NodeAttributeServiceBean implements NodeAttributeService,
		NodeAttributeServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(NodeAttributeServiceBean.class);

	@EJB
	private ProxyAttributeService proxyAttributeService;

	@RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
	public Object getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, AttributeTypeNotFoundException {
		LOG.debug("get attribute " + attributeName + " for login "
				+ subjectLogin);
		return this.proxyAttributeService.getAttributeValue(subjectLogin,
				attributeName);
	}
}
