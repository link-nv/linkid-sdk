/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeService;
import net.link.safeonline.authentication.service.AttributeServiceRemote;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.model.ApplicationManager;

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
public class AttributeServiceBean implements AttributeService,
		AttributeServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(AttributeServiceBean.class);

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private ApplicationManager applicationManager;

	@EJB
	private ApplicationIdentityDAO applicationIdentityDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectDAO subjectDAO;

	@SuppressWarnings("unchecked")
	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public Object getConfirmedAttributeValue(String subjectLogin,
			String attributeName) throws AttributeNotFoundException,
			PermissionDeniedException, SubjectNotFoundException {
		LOG.debug("get attribute " + attributeName + " for login "
				+ subjectLogin);

		List<ApplicationIdentityAttributeEntity> confirmedAttributes = getConfirmedIdentityAttributes(subjectLogin);

		AttributeTypeEntity attributeType = checkAttributeReadPermission(
				attributeName, confirmedAttributes);

		if (attributeType.isMultivalued()) {
			SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);
			List<AttributeEntity> attributes = this.attributeDAO
					.listAttributes(subject, attributeType);
			LOG.debug("number of attributes for attrib type "
					+ attributeType.getName() + ": " + attributes.size());
			DatatypeType datatype = attributeType.getType();
			switch (datatype) {
			case STRING: {
				String[] values = new String[attributes.size()];
				for (int idx = 0; idx < values.length; idx++) {
					values[idx] = attributes.get(idx).getStringValue();
				}
				return values;
			}
			case BOOLEAN: {
				Boolean[] values = new Boolean[attributes.size()];
				for (int idx = 0; idx < values.length; idx++) {
					values[idx] = attributes.get(idx).getBooleanValue();
				}
				return values;
			}
			case COMPOUNDED: {
				Map[] values = new Map[attributes.size()];
				for (CompoundedAttributeTypeMemberEntity member : attributeType
						.getMembers()) {
					AttributeTypeEntity memberAttributeType = member
							.getMember();
					for (int idx = 0; idx < attributes.size(); idx++) {
						AttributeEntity attribute = this.attributeDAO
								.findAttribute(subject, memberAttributeType,
										idx);
						Map<String, Object> memberMap = values[idx];
						if (null == memberMap) {
							memberMap = new HashMap<String, Object>();
							values[idx] = memberMap;
						}
						Object value = null;
						if (null != attribute) {
							switch (memberAttributeType.getType()) {
							case STRING:
								value = attribute.getStringValue();
								break;
							case BOOLEAN:
								value = attribute.getBooleanValue();
								break;
							default:
								throw new EJBException(
										"datatype not supported: "
												+ memberAttributeType.getType());
							}
						}
						memberMap.put(memberAttributeType.getName(), value);
					}
				}
				return values;
			}
			default:
				throw new EJBException("datatype not supported: " + datatype);
			}
		}

		// else single-valued attribute

		AttributeEntity attribute = this.attributeDAO.getAttribute(
				attributeName, subjectLogin);

		DatatypeType datatype = attribute.getAttributeType().getType();
		switch (datatype) {
		case STRING: {
			String value = attribute.getStringValue();
			return value;
		}
		case BOOLEAN: {
			Boolean value = attribute.getBooleanValue();
			return value;
		}
		default:
			throw new EJBException("datatype not supported: " + datatype);
		}
	}

	private AttributeTypeEntity checkAttributeReadPermission(
			String attributeName,
			List<ApplicationIdentityAttributeEntity> attributes)
			throws PermissionDeniedException {
		for (ApplicationIdentityAttributeEntity attribute : attributes) {
			LOG
					.debug("identity attribute: "
							+ attribute.getAttributeTypeName());
			if (attribute.getAttributeTypeName().equals(attributeName)) {
				return attribute.getAttributeType();
			}
		}
		LOG.debug("attribute not in set of confirmed identity attributes");
		throw new PermissionDeniedException();
	}

	private List<ApplicationIdentityAttributeEntity> getConfirmedIdentityAttributes(
			String subjectLogin) throws SubjectNotFoundException,
			PermissionDeniedException {
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);
		ApplicationEntity application = this.applicationManager
				.getCallerApplication();

		/*
		 * The subject needs to be subscribed onto this application.
		 */
		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			LOG.debug("subject is not subscribed");
			throw new PermissionDeniedException();
		}

		/*
		 * The subject needs to have a confirmed identity version.
		 */
		Long confirmedIdentityVersion = subscription
				.getConfirmedIdentityVersion();
		if (null == confirmedIdentityVersion) {
			LOG.debug("subject has no confirmed identity version");
			throw new PermissionDeniedException();
		}

		ApplicationIdentityEntity confirmedApplicationIdentity;
		try {
			confirmedApplicationIdentity = this.applicationIdentityDAO
					.getApplicationIdentity(application,
							confirmedIdentityVersion);
		} catch (ApplicationIdentityNotFoundException e) {
			throw new EJBException(
					"application identity not found for version: "
							+ confirmedIdentityVersion);
		}

		/*
		 * Filter the datamining attributes
		 */
		List<ApplicationIdentityAttributeEntity> attributes = new ArrayList<ApplicationIdentityAttributeEntity>();
		for (ApplicationIdentityAttributeEntity attribute : confirmedApplicationIdentity
				.getAttributes()) {
			if (!attribute.isDataMining()) {
				attributes.add(attribute);
			}
		}
		return attributes;
	}

	@SuppressWarnings("unchecked")
	@RolesAllowed(SafeOnlineApplicationRoles.APPLICATION_ROLE)
	public Map<String, Object> getConfirmedAttributeValues(String subjectLogin)
			throws SubjectNotFoundException, PermissionDeniedException {
		LOG.debug("get confirmed attributes for subject: " + subjectLogin);
		List<ApplicationIdentityAttributeEntity> confirmedAttributes = getConfirmedIdentityAttributes(subjectLogin);
		Map<String, Object> resultAttributes = new TreeMap<String, Object>();
		SubjectEntity subject = this.subjectDAO.getSubject(subjectLogin);
		for (ApplicationIdentityAttributeEntity confirmedAttribute : confirmedAttributes) {
			AttributeTypeEntity attributeType = confirmedAttribute
					.getAttributeType();
			String attributeName = confirmedAttribute.getAttributeTypeName();
			List<AttributeEntity> attributes = this.attributeDAO
					.listAttributes(subject, attributeType);
			if (attributes.isEmpty()) {
				continue;
			}
			LOG.debug("confirmed attribute: " + attributeName);
			DatatypeType datatype = attributeType.getType();
			Object value;
			if (attributeType.isMultivalued()) {
				switch (datatype) {
				case STRING: {
					String[] values = new String[attributes.size()];
					for (int idx = 0; idx < values.length; idx++) {
						values[idx] = attributes.get(idx).getStringValue();
					}
					value = values;
					break;
				}
				case BOOLEAN: {
					Boolean[] values = new Boolean[attributes.size()];
					for (int idx = 0; idx < values.length; idx++) {
						values[idx] = attributes.get(idx).getBooleanValue();
					}
					value = values;
					break;
				}
				case COMPOUNDED: {
					Map[] values = new Map[attributes.size()];
					for (CompoundedAttributeTypeMemberEntity member : attributeType
							.getMembers()) {
						AttributeTypeEntity memberAttributeType = member
								.getMember();
						for (int idx = 0; idx < attributes.size(); idx++) {
							AttributeEntity attribute = this.attributeDAO
									.findAttribute(subject,
											memberAttributeType, idx);
							Map<String, Object> memberMap = values[idx];
							if (null == memberMap) {
								memberMap = new HashMap<String, Object>();
								values[idx] = memberMap;
							}
							Object memberValue = null;
							if (null != attribute) {
								switch (memberAttributeType.getType()) {
								case STRING:
									memberValue = attribute.getStringValue();
									break;
								case BOOLEAN:
									memberValue = attribute.getBooleanValue();
									break;
								default:
									throw new EJBException(
											"datatype not supported: "
													+ memberAttributeType
															.getType());
								}
							}
							memberMap.put(memberAttributeType.getName(),
									memberValue);
						}
					}
					value = values;
					break;
				}
				default:
					throw new EJBException("datatype not supported: "
							+ datatype);
				}
			} else {
				AttributeEntity attribute = attributes.get(0);
				switch (datatype) {
				case STRING:
					value = attribute.getStringValue();
					break;
				case BOOLEAN:
					value = attribute.getBooleanValue();
					break;
				default:
					throw new EJBException("datatype not supported: "
							+ datatype);
				}
			}

			resultAttributes.put(attributeName, value);
		}
		return resultAttributes;
	}
}
