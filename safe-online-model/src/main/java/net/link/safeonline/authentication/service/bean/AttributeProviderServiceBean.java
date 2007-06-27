/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
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
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
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

		if (false == attributeType.isCompounded()) {
			return attributes;
		}

		List<CompoundedAttributeTypeMemberEntity> members = attributeType
				.getMembers();
		for (AttributeEntity attribute : attributes) {
			for (CompoundedAttributeTypeMemberEntity member : members) {
				AttributeEntity memberAttribute = this.attributeDAO
						.findAttribute(subject, member.getMember(), attribute
								.getAttributeIndex());
				if (null != memberAttribute) {
					attribute.getMember().add(memberAttribute);
				}
			}
		}

		return attributes;
	}

	/**
	 * Check whether the caller application is an attribute provider for the
	 * given attribute type.
	 * 
	 * <p>
	 * It's an interesting design-pattern to combine access control checking
	 * with retrieval of required entities for further processing. That way
	 * you're always sure that the checks have been executed.
	 * </p>
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
			Object attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException,
			DatatypeMismatchException {
		LOG
				.debug("create attribute: " + attributeName + " for "
						+ subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		if (null == attributeValue) {
			this.attributeDAO.addAttribute(attributeType, subject);
			return;
		}

		Class attributeValueClass = attributeValue.getClass();
		if (attributeType.isMultivalued()) {
			if (false == attributeValueClass.isArray()) {
				throw new DatatypeMismatchException();
			}

			int size = Array.getLength(attributeValue);
			for (int idx = 0; idx < size; idx++) {
				Object value = Array.get(attributeValue, idx);
				AttributeEntity attribute = this.attributeDAO.addAttribute(
						attributeType, subject);
				setAttributeValue(attribute, value);
			}
		} else {
			/*
			 * Single-valued attribute.
			 */
			AttributeEntity attribute = this.attributeDAO.addAttribute(
					attributeType, subject);
			setAttributeValue(attribute, attributeValue);
		}
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void setAttribute(String subjectLogin, String attributeName,
			Object attributeValue) throws AttributeTypeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException,
			AttributeNotFoundException, DatatypeMismatchException {
		LOG.debug("set attribute " + attributeName + " for " + subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		if (attributeType.isMultivalued()) {
			List<AttributeEntity> attributes = this.attributeDAO
					.listAttributes(subject, attributeType);
			if (attributes.isEmpty()) {
				/*
				 * Via setAttribute one can only update existing multivalued
				 * attributes, not create them.
				 */
				throw new AttributeNotFoundException();
			}
			if (null == attributeValue) {
				/*
				 * In this case we remove all but one, which we set with a null
				 * value.
				 */
				Iterator<AttributeEntity> iterator = attributes.iterator();
				AttributeEntity attribute = iterator.next();
				clearAttributeValues(attribute);
				while (iterator.hasNext()) {
					attribute = iterator.next();
					this.attributeDAO.removeAttribute(attribute);
				}
			} else {
				if (false == attributeValue.getClass().isArray()) {
					throw new DatatypeMismatchException();
				}
				int newSize = Array.getLength(attributeValue);
				Iterator<AttributeEntity> iterator = attributes.iterator();
				for (int idx = 0; idx < newSize; idx++) {
					Object value = Array.get(attributeValue, idx);
					AttributeEntity attribute;
					if (iterator.hasNext()) {
						attribute = iterator.next();
					} else {
						attribute = this.attributeDAO.addAttribute(
								attributeType, subject);
					}
					setAttributeValue(attribute, value);
				}
				while (iterator.hasNext()) {
					AttributeEntity attribute = iterator.next();
					this.attributeDAO.removeAttribute(attribute);
				}
			}
		} else {
			/*
			 * Single-valued attribute.
			 */
			AttributeEntity attribute = this.attributeDAO.getAttribute(
					attributeType, subject);

			if (null == attributeValue) {
				/*
				 * In case the attribute value is null we cannot extract the
				 * reflection class type. But actually we don't care. Just clear
				 * all.
				 */
				clearAttributeValues(attribute);
				return;
			}

			setAttributeValue(attribute, attributeValue);
		}
	}

	/**
	 * Generic datatype conversion method with type-checking.
	 * 
	 * TODO: maybe we could move this method to AttributeEntity itself?
	 * 
	 * @param attribute
	 * @param value
	 * @throws DatatypeMismatchException
	 */
	private void setAttributeValue(AttributeEntity attribute, Object value)
			throws DatatypeMismatchException {
		AttributeTypeEntity attributeType = attribute.getAttributeType();
		DatatypeType datatype = attributeType.getType();
		switch (datatype) {
		case STRING:
			if (false == value instanceof String) {
				throw new DatatypeMismatchException();
			}
			String stringValue = (String) value;
			attribute.setStringValue(stringValue);
			return;
		case BOOLEAN:
			if (false == value instanceof Boolean) {
				throw new DatatypeMismatchException();
			}
			Boolean booleanValue = (Boolean) value;
			attribute.setBooleanValue(booleanValue);
			return;
		default:
			throw new EJBException("datatype not supported: " + datatype);
		}
	}

	private void clearAttributeValues(AttributeEntity attribute) {
		attribute.setStringValue(null);
		attribute.setBooleanValue(null);
	}
}
