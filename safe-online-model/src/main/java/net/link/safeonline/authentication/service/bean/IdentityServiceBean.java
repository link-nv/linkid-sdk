/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class IdentityServiceBean implements IdentityService {

	private static final Log LOG = LogFactory.getLog(IdentityServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String getName() {
		String login = this.subjectManager.getCallerLogin();

		AttributeEntity nameAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.NAME_ATTRIBUTE, login);
		if (null == nameAttribute) {
			return null;
		}

		String name = nameAttribute.getStringValue();

		LOG.debug("get name of " + login + ": " + name);
		return name;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void saveName(String name) {
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("save name " + name + " for entity with login " + login);

		AttributeEntity nameAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.NAME_ATTRIBUTE, login);
		if (null == nameAttribute) {
			this.attributeDAO.addAttribute(SafeOnlineConstants.NAME_ATTRIBUTE,
					login, name);
			return;
		}

		nameAttribute.setStringValue(name);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<HistoryEntity> getHistory() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<HistoryEntity> result = this.historyDAO.getHistory(subject);
		return result;
	}
}
