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
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
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

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<HistoryEntity> getHistory() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		List<HistoryEntity> result = this.historyDAO.getHistory(subject);
		return result;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String findAttribute(String attributeName)
			throws PermissionDeniedException {
		String subjectLogin = this.subjectManager.getCallerLogin();
		LOG.debug("get attribute " + attributeName + " for user with login "
				+ subjectLogin);
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, subjectLogin);
		if (null == attribute) {
			return null;
		}
		if (false == attribute.getAttributeType().isUserVisible()) {
			throw new PermissionDeniedException();
		}
		String value = attribute.getStringValue();
		return value;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void saveAttribute(String attributeName, String attributeValue)
			throws PermissionDeniedException {
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("save attribute " + attributeName + " for entity with login "
				+ login);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.findAttributeType(attributeName);
		if (null == attributeType) {
			throw new IllegalArgumentException("attribute type not found: "
					+ attributeName);
		}
		if (false == attributeType.isUserEditable()) {
			throw new PermissionDeniedException();
		}

		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, login);
		if (null == attribute) {
			this.attributeDAO
					.addAttribute(attributeName, login, attributeValue);
			return;
		}
		attribute.setStringValue(attributeValue);
	}
}
