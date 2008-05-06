package net.link.safeonline.authentication.service.bean;

import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.ProxyAttributeServiceRemote;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceRegistrationDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class ProxyAttributeServiceBean implements ProxyAttributeService,
		ProxyAttributeServiceRemote {

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SubjectService subjectService;

	@EJB
	private DeviceRegistrationDAO registeredDeviceDAO;

	private static final Log LOG = LogFactory
			.getLog(ProxyAttributeServiceBean.class);

	public Object findDeviceAttributeValue(String deviceUserId,
			String attributeName) throws AttributeTypeNotFoundException,
			PermissionDeniedException {

		LOG.debug("find device attribute " + attributeName + " for "
				+ deviceUserId);
		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);

		if (isLocalAttribute(attributeType))
			return findLocalAttribute(deviceUserId, attributeType);

		return findRemoteAttribute(deviceUserId, attributeType);

	}

	public Object findAttributeValue(String userId, String attributeName)
			throws PermissionDeniedException, AttributeTypeNotFoundException {

		LOG.debug("find attribute " + attributeName + " for " + userId);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(attributeName);

		SubjectEntity subject = this.subjectService.findSubject(userId);
		if (null == subject)
			return null;

		String subjectId = userId;
		if (attributeType.isDeviceAttribute())
			subjectId = getDeviceId(subject, attributeType);

		if (attributeType.isLocal())
			return findLocalAttribute(subjectId, attributeType);

		return findRemoteAttribute(subjectId, attributeType);
	}

	private String getDeviceId(SubjectEntity subject,
			AttributeTypeEntity attributeType) {

		List<DeviceRegistrationEntity> registeredDevices = this.registeredDeviceDAO
				.listRegisteredDevices(subject);

		for (DeviceRegistrationEntity registeredDevice : registeredDevices) {
			AttributeTypeEntity deviceAttributeType = registeredDevice
					.getDevice().getAttributeType();

			if (deviceAttributeType.getName().equals(attributeType.getName()))
				return registeredDevice.getId();

			if (deviceAttributeType.isCompounded()) {
				List<CompoundedAttributeTypeMemberEntity> members = deviceAttributeType
						.getMembers();
				for (CompoundedAttributeTypeMemberEntity member : members)
					if (member.getMember().getName().equals(
							attributeType.getName()))
						return registeredDevice.getId();
			}
		}

		return subject.getUserId();
	}

	private boolean isLocalAttribute(AttributeTypeEntity attributeType) {

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();

		if (null == attributeType.getLocation())
			return true;

		if (authIdentityServiceClient.getCertificate().equals(
				attributeType.getLocation().getAuthnCertificate()))
			return true;

		return false;
	}

	private Object findLocalAttribute(String subjectId,
			AttributeTypeEntity attributeType) {

		LOG.debug("find local attribute " + attributeType.getName() + " for "
				+ subjectId);

		SubjectEntity subject = this.subjectService.findSubject(subjectId);
		if (null == subject)
			return null;

		// filter out the empty attributes
		List<AttributeEntity> attributes = this.attributeDAO.listAttributes(
				subject, attributeType);
		List<AttributeEntity> nonEmptyAttributes = new LinkedList<AttributeEntity>();
		for (AttributeEntity attribute : attributes)
			if (attribute.getAttributeType().isCompounded())
				nonEmptyAttributes.add(attribute);
			else if (!attribute.isEmpty())
				nonEmptyAttributes.add(attribute);

		LOG.debug("found " + nonEmptyAttributes.size());

		if (nonEmptyAttributes.isEmpty())
			return null;

		return getValue(nonEmptyAttributes, attributeType, subject);
	}

	private Object findRemoteAttribute(String subjectId,
			AttributeTypeEntity attributeType) throws PermissionDeniedException {

		LOG.debug("find remote attribute " + attributeType.getName() + " for "
				+ subjectId);

		AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();
		AttributeClient attributeClient = new AttributeClientImpl(attributeType
				.getLocation().getLocation(), authIdentityServiceClient
				.getCertificate(), authIdentityServiceClient.getPrivateKey());

		DatatypeType datatype = attributeType.getType();
		Class<?> attributeClass;

		if (attributeType.isMultivalued())
			switch (datatype) {
			case STRING:
			case LOGIN:
				attributeClass = String[].class;
				break;
			case BOOLEAN:
				attributeClass = Boolean[].class;
				break;
			case INTEGER:
				attributeClass = Integer[].class;
				break;
			case DOUBLE:
				attributeClass = Double[].class;
				break;
			case DATE:
				attributeClass = Date[].class;
				break;
			case COMPOUNDED:
				attributeClass = Map[].class;
				break;
			default:
				throw new EJBException("datatype not supported: " + datatype);
			}
		else
			switch (datatype) {
			case STRING:
			case LOGIN:
				attributeClass = String.class;
				break;
			case BOOLEAN:
				attributeClass = Boolean.class;
				break;
			case INTEGER:
				attributeClass = Integer.class;
				break;
			case DOUBLE:
				attributeClass = Double.class;
				break;
			case DATE:
				attributeClass = Date.class;
				break;
			case COMPOUNDED:
				attributeClass = Map.class;
				break;
			default:
				throw new EJBException("datatype not supported: " + datatype);
			}

		try {
			return attributeClient.getAttributeValue(subjectId, attributeType
					.getName(), attributeClass);
		}

		catch (ConnectException e) {
			throw new PermissionDeniedException(e.getMessage());
		} catch (RequestDeniedException e) {
			throw new PermissionDeniedException(e.getMessage());
		} catch (net.link.safeonline.sdk.exception.AttributeNotFoundException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Object getValue(List<AttributeEntity> attributes,
			AttributeTypeEntity attributeType, SubjectEntity subject) {

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
			return null;

		AttributeEntity attribute = attributes.get(0);
		return attribute.getValue();
	}
}
