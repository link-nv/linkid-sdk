/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.authentication.service.AttributeProviderServiceRemote;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.ApplicationManager;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN)
public class AttributeProviderServiceBean implements AttributeProviderService,
		AttributeProviderServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(AttributeProviderServiceBean.class);

	@EJB
	private AttributeProviderDAO attributeProviderDAO;

	@EJB
	private ApplicationManager applicationManager;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SubjectDAO subjectDAO;

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public List<AttributeEntity> getAttributes(String subjectLogin,
			String attributeName) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException {

		LOG.debug("get attributes of type " + attributeName + " for subject "
				+ subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		List<AttributeEntity> attributes = this.attributeDAO.listAttributes(
				subject, attributeType);
		return attributes;
	}

	/**
	 * Check whether the caller application is an attribute provider for the
	 * given attribute type.
	 * 
	 * @param attributeName
	 * @return
	 * @throws AttributeTypeNotFoundException
	 * @throws PermissionDeniedException
	 */
	private AttributeTypeEntity checkAttributeProviderPermission(
			String attributeName) throws AttributeTypeNotFoundException,
			PermissionDeniedException {
		ApplicationEntity application = this.applicationManager
				.getCallerApplication();
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);
		AttributeProviderEntity attributeProvider = this.attributeProviderDAO
				.findAttributeProvider(application, attributeType);
		if (null == attributeProvider) {
			throw new PermissionDeniedException();
		}
		return attributeType;
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void createAttribute(String subjectLogin, String attributeName,
			String attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException {
		LOG
				.debug("create attribute: " + attributeName + " for "
						+ subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		this.attributeDAO.addAttribute(attributeType, subject, attributeValue);
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void setAttribute(String subjectLogin, String attributeName,
			Object attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException,
			AttributeNotFoundException, DatatypeMismatchException {
		LOG.debug("set attribute " + attributeName + " for " + subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		AttributeEntity attribute = this.attributeDAO.getAttribute(
				attributeType, subject);

		if (null == attributeValue) {
			/*
			 * In case the attribute value is null we cannot extract the
			 * reflection class type. But actually we don't care. Just clear
			 * all.
			 */
			attribute.setStringValue(null);
			attribute.setBooleanValue(null);
			return;
		}

		String datatype = attributeType.getType();
		if (SafeOnlineConstants.STRING_TYPE.equals(datatype)) {
			if (false == attributeValue instanceof String) {
				throw new DatatypeMismatchException();
			}
			String stringValue = (String) attributeValue;
			attribute.setStringValue(stringValue);
			return;
		}

		if (SafeOnlineConstants.BOOLEAN_TYPE.equals(datatype)) {
			if (false == attributeValue instanceof Boolean) {
				throw new DatatypeMismatchException();
			}
			Boolean booleanValue = (Boolean) attributeValue;
			attribute.setBooleanValue(booleanValue);
			return;
		}
	}
}
