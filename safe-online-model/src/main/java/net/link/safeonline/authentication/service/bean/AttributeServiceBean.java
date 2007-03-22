/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Attribute Service Implementation for applications.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
public class AttributeServiceBean implements AttributeService {

	private static final Log LOG = LogFactory
			.getLog(AttributeServiceBean.class);

	@EJB
	private AttributeDAO attributeDAO;

	@Resource
	private SessionContext context;

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public String getAttribute(String subjectLogin, String attributeName)
			throws AttributeNotFoundException {
		LOG.debug("get attribute " + attributeName + " for login "
				+ subjectLogin);

		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, subjectLogin);
		if (null == attribute) {
			throw new AttributeNotFoundException();
		}

		// TODO: check agains application identity
		Principal callerPrincipal = this.context.getCallerPrincipal();
		LOG.debug("session context caller principal name: "
				+ callerPrincipal.getName());

		String value = attribute.getStringValue();
		return value;
	}
}
