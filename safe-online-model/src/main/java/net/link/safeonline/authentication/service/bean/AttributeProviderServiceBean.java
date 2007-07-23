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
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

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
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.ApplicationManager;
import net.link.safeonline.model.bean.AttributeManagerLWBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

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

	private AttributeManagerLWBean attributeManager;

	@PostConstruct
	public void postConstructCallback() {
		/*
		 * By injecting the attribute DAO of this session bean in the attribute
		 * manager we are sure that the attribute manager (a lightweight bean)
		 * will live within the same transaction and security context as this
		 * identity service EJB3 session bean.
		 */
		LOG.debug("postConstruct");
		this.attributeManager = new AttributeManagerLWBean(this.attributeDAO);
	}

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
					attribute.getMembers().add(memberAttribute);
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

	private void createCompoundAttribute(SubjectEntity subject,
			AttributeTypeEntity attributeType, Map<String, Object> memberValues) {
		AttributeEntity compoundAttribute = this.attributeDAO.addAttribute(
				attributeType, subject);
		long attributeIdx = compoundAttribute.getAttributeIndex();
		LOG.debug("createCompoundAttribute: idx " + attributeIdx);
		String attributeId = UUID.randomUUID().toString();
		LOG.debug("new attribute Id: " + attributeId);
		compoundAttribute.setStringValue(attributeId);

		List<CompoundedAttributeTypeMemberEntity> members = attributeType
				.getMembers();
		for (CompoundedAttributeTypeMemberEntity member : members) {
			AttributeTypeEntity memberAttributeType = member.getMember();
			AttributeEntity memberAttribute = this.attributeDAO.addAttribute(
					memberAttributeType, subject, attributeIdx);
			Object attributeValue = memberValues.get(memberAttributeType
					.getName());
			memberAttribute.setValue(attributeValue);
		}
	}

	@SuppressWarnings("unchecked")
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

		if (attributeValue instanceof Map) {
			Map<String, Object> memberValues = (Map<String, Object>) attributeValue;
			createCompoundAttribute(subject, attributeType, memberValues);
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

	private void setAttributeValue(AttributeEntity attribute, Object value)
			throws DatatypeMismatchException {
		try {
			attribute.setValue(value);
		} catch (ClassCastException e) {
			throw new DatatypeMismatchException();
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
			setMultivaluedAttribute(attributeValue, attributeType, subject);
		} else {
			setSinglevaluedAttribute(attributeValue, attributeType, subject);
		}
	}

	private void setSinglevaluedAttribute(Object attributeValue,
			AttributeTypeEntity attributeType, SubjectEntity subject)
			throws AttributeNotFoundException, DatatypeMismatchException {
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

	private void setMultivaluedAttribute(Object attributeValue,
			AttributeTypeEntity attributeType, SubjectEntity subject)
			throws AttributeNotFoundException, DatatypeMismatchException {
		List<AttributeEntity> attributes = this.attributeDAO.listAttributes(
				subject, attributeType);
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
					attribute = this.attributeDAO.addAttribute(attributeType,
							subject);
				}
				setAttributeValue(attribute, value);
			}
			while (iterator.hasNext()) {
				AttributeEntity attribute = iterator.next();
				this.attributeDAO.removeAttribute(attribute);
			}
		}
	}

	private void clearAttributeValues(AttributeEntity attribute) {
		attribute.setStringValue(null);
		attribute.setBooleanValue(null);
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void setCompoundAttributeRecord(String subjectLogin,
			String attributeName, String attributeId,
			Map<String, Object> memberValues)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, DatatypeMismatchException,
			AttributeNotFoundException {
		LOG.debug("set compound attribute " + attributeName + " for "
				+ subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		if (false == attributeType.isCompounded()) {
			throw new DatatypeMismatchException();
		}

		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		/*
		 * AttributeId is the global Id of the record, while AttributeIdx is the
		 * local database Id of the attribute record.
		 */
		AttributeEntity compoundAttribute = getCompoundAttribute(subject,
				attributeType, attributeId);

		long attributeIdx = compoundAttribute.getAttributeIndex();
		LOG.debug("attribute idx: " + attributeIdx);

		for (Map.Entry<String, Object> memberValue : memberValues.entrySet()) {
			AttributeTypeEntity memberAttributeType = this.attributeTypeDAO
					.getAttributeType(memberValue.getKey());
			AttributeEntity memberAttribute = this.attributeDAO.getAttribute(
					memberAttributeType, subject, attributeIdx);
			setAttributeValue(memberAttribute, memberValue.getValue());
		}
	}

	private AttributeEntity getCompoundAttribute(SubjectEntity subject,
			AttributeTypeEntity attributeType, String attributeId)
			throws AttributeNotFoundException {
		List<AttributeEntity> compoundAttributes = this.attributeDAO
				.listAttributes(subject, attributeType);
		for (AttributeEntity compoundAttribute : compoundAttributes) {
			if (attributeId.equals(compoundAttribute.getStringValue())) {
				return compoundAttribute;
			}
		}
		throw new AttributeNotFoundException();
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void removeAttribute(String subjectLogin, String attributeName)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, AttributeNotFoundException {
		LOG.debug("remove attribute " + attributeName + " from subject "
				+ subjectLogin);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		this.attributeManager.removeAttribute(attributeType, subject);
	}

	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public void removeCompoundAttributeRecord(String subjectLogin,
			String attributeName, String attributeId)
			throws AttributeTypeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, AttributeNotFoundException {
		LOG.debug("remove compound attribute " + attributeName
				+ " from subject " + subjectLogin + " with attrib Id "
				+ attributeId);
		AttributeTypeEntity attributeType = checkAttributeProviderPermission(attributeName);
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);

		this.attributeManager.removeCompoundAttribute(attributeType, subject,
				attributeId);
	}
}
