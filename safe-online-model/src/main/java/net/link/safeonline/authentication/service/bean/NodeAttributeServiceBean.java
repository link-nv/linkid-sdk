/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
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
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Attribute Service Implementation for nodes.
 * 
 * @author fcorneli
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
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private SubjectService subjectService;

	@RolesAllowed(SafeOnlineNodeRoles.NODE_ROLE)
	public Object getAttributeValue(String subjectLogin, String attributeName)
			throws AttributeNotFoundException, PermissionDeniedException,
			SubjectNotFoundException, AttributeTypeNotFoundException {
		LOG.debug("get attribute " + attributeName + " for login "
				+ subjectLogin);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);
		SubjectEntity subject = this.subjectService.getSubject(subjectLogin);
		List<AttributeEntity> attributes = this.attributeDAO.listAttributes(
				subject, attributeType);

		Object value = getValue(attributes, attributeType, subject);
		return value;
	}

	@SuppressWarnings("unchecked")
	private Object getValue(List<AttributeEntity> attributes,
			AttributeTypeEntity attributeType, SubjectEntity subject)
			throws AttributeNotFoundException {
		DatatypeType datatype = attributeType.getType();
		if (attributeType.isMultivalued())
			switch (datatype) {
			case STRING:
			case LOGIN: {
				String[] values = new String[attributes.size()];
				for (int idx = 0; idx < values.length; idx++)
					values[idx] = attributes.get(idx).getStringValue();
				return values;
			}
			case BOOLEAN: {
				Boolean[] values = new Boolean[attributes.size()];
				for (int idx = 0; idx < values.length; idx++)
					values[idx] = attributes.get(idx).getBooleanValue();
				return values;
			}
			case INTEGER: {
				Integer[] values = new Integer[attributes.size()];
				for (int idx = 0; idx < values.length; idx++)
					values[idx] = attributes.get(idx).getIntegerValue();
				return values;
			}
			case DOUBLE: {
				Double[] values = new Double[attributes.size()];
				for (int idx = 0; idx < values.length; idx++)
					values[idx] = attributes.get(idx).getDoubleValue();
				return values;
			}
			case DATE: {
				Date[] values = new Date[attributes.size()];
				for (int idx = 0; idx < values.length; idx++)
					values[idx] = attributes.get(idx).getDateValue();
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
						Object memberValue;
						if (null != attribute)
							memberValue = attribute.getValue();
						else
							memberValue = null;
						memberMap.put(memberAttributeType.getName(),
								memberValue);
					}
				}
				return values;
			}
			default:
				throw new EJBException("datatype not supported: " + datatype);
			}
		/*
		 * Single-valued attribute.
		 */
		if (attributes.isEmpty())
			throw new AttributeNotFoundException();
		AttributeEntity attribute = attributes.get(0);
		return attribute.getValue();
	}
}
