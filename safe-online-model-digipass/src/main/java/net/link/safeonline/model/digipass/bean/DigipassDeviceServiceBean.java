/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.digipass.bean;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.digipass.keystore.DigipassKeyStoreUtils;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.model.digipass.DigipassDeviceServiceRemote;
import net.link.safeonline.model.digipass.DigipassException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DigipassDeviceServiceBean implements DigipassDeviceService,
		DigipassDeviceServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(DigipassDeviceServiceBean.class);

	@EJB
	private SubjectService subjectService;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	public String authenticate(String loginName, String token)
			throws SubjectNotFoundException, PermissionDeniedException {
		NameIdentifierMappingClient idMappingClient = getIDMappingClient();
		String deviceUserId;
		try {
			deviceUserId = idMappingClient.getUserId(loginName);
		} catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
			LOG.debug("subject not found: " + loginName);
			throw new SubjectNotFoundException();
		} catch (RequestDeniedException e) {
			LOG.debug("request denied: " + e.getMessage());
			throw new PermissionDeniedException("Unable to retrieve login: "
					+ loginName);
		}
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);
		if (0 == deviceSubject.getRegistrations().size()) {
			return null;
		}
		if (Integer.parseInt(token) % 2 != 0) {
			LOG.debug("Invalid token: " + token);
			this.historyDAO
					.addHistoryEntry(new Date(), deviceSubject
							.getRegistrations().get(0),
							HistoryEventType.LOGIN_INCORRECT_DIGIPASS_TOKEN,
							null, null);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, deviceSubject
							.getRegistrations().get(0).getUserId(),
					"incorrect digipass token");
			return null;
		}
		return deviceUserId;
	}

	public String register(String loginName, String serialNumber)
			throws SubjectNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException {

		NameIdentifierMappingClient idMappingClient = getIDMappingClient();
		String deviceUserId;
		try {
			deviceUserId = idMappingClient.getUserId(loginName);
		} catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
			LOG.debug("subject not found: " + loginName);
			throw new SubjectNotFoundException();
		} catch (RequestDeniedException e) {
			LOG.debug("request denied: " + e.getMessage());
			throw new PermissionDeniedException("Unable to retrieve login: "
					+ loginName);
		}

		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN,
						serialNumber);
		if (null != existingMappedSubject) {
			throw new ArgumentIntegrityException();
		}

		DeviceSubjectEntity deviceSubject = this.subjectService
				.findDeviceSubject(deviceUserId);
		if (null == deviceSubject) {
			deviceSubject = this.subjectService.addDeviceSubject(deviceUserId);
		}
		SubjectEntity deviceRegistration = this.subjectService
				.addDeviceRegistration();
		deviceSubject.getRegistrations().add(deviceRegistration);
		setSerialNumber(deviceRegistration, serialNumber);

		this.subjectIdentifierDAO.addSubjectIdentifier(
				DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber,
				deviceRegistration);
		return deviceUserId;
	}

	private void setSerialNumber(SubjectEntity subject, String serialNumber) {
		AttributeTypeEntity snAttributeType;
		try {
			snAttributeType = this.attributeTypeDAO
					.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException(
					"digipass serial number attribute type not found");
		}
		List<AttributeEntity> mobileAttributes = this.attributeDAO
				.listAttributes(subject, snAttributeType);
		AttributeEntity mobileAttribute = this.attributeDAO.addAttribute(
				snAttributeType, subject, mobileAttributes.size());
		mobileAttribute.setStringValue(serialNumber);
	}

	public void remove(String loginName, String serialNumber)
			throws SubjectNotFoundException, DigipassException,
			PermissionDeniedException {
		NameIdentifierMappingClient idMappingClient = getIDMappingClient();
		String deviceUserId;
		try {
			deviceUserId = idMappingClient.getUserId(loginName);
		} catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
			LOG.debug("subject not found: " + loginName);
			throw new SubjectNotFoundException();
		} catch (RequestDeniedException e) {
			LOG.debug("request denied: " + e.getMessage());
			throw new PermissionDeniedException("Unable to retrieve login: "
					+ loginName);
		}

		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);
		SubjectEntity deviceRegistration = this.subjectIdentifierDAO
				.findSubject(DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN,
						serialNumber);
		if (null == deviceRegistration)
			throw new DigipassException("device registration not found");

		AttributeTypeEntity snAttributeType;
		try {
			snAttributeType = this.attributeTypeDAO
					.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException(
					"digipass serial number attribute type not found");
		}
		this.subjectIdentifierDAO.removeSubjectIdentifier(deviceRegistration,
				DigipassConstants.DIGIPASS_IDENTIFIER_DOMAIN, serialNumber);
		List<AttributeEntity> snAttributes = this.attributeDAO.listAttributes(
				deviceRegistration, snAttributeType);
		for (AttributeEntity snAttribute : snAttributes) {
			if (snAttribute.getStringValue().equals(serialNumber))
				this.attributeDAO.removeAttribute(snAttribute);
		}
		deviceSubject.getRegistrations().remove(deviceRegistration);
	}

	public List<AttributeDO> getDigipasses(String loginName, Locale locale)
			throws SubjectNotFoundException, PermissionDeniedException {
		LOG.debug("get digipasses for: " + loginName);
		NameIdentifierMappingClient idMappingClient = getIDMappingClient();
		String deviceUserId;
		try {
			deviceUserId = idMappingClient.getUserId(loginName);
		} catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
			LOG.debug("subject not found: " + loginName);
			throw new SubjectNotFoundException();
		} catch (RequestDeniedException e) {
			LOG.debug("request denied: " + e.getMessage());
			throw new PermissionDeniedException("Unable to retrieve login: "
					+ loginName);
		}

		AttributeTypeEntity snAttributeType;
		try {
			snAttributeType = this.attributeTypeDAO
					.getAttributeType(DigipassConstants.DIGIPASS_SN_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException(
					"digipass serial number attribute type not found");
		}

		AttributeTypeDescriptionEntity attributeTypeDescription = findAttributeTypeDescription(
				snAttributeType, locale);
		String humanReadableName = null;
		String description = null;
		if (null != attributeTypeDescription) {
			humanReadableName = attributeTypeDescription.getName();
			description = attributeTypeDescription.getDescription();
		}

		List<AttributeDO> snList = new LinkedList<AttributeDO>();
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);
		for (SubjectEntity deviceRegistration : deviceSubject
				.getRegistrations()) {
			List<AttributeEntity> attributes = this.attributeDAO
					.listAttributes(deviceRegistration, snAttributeType);
			for (AttributeEntity attribute : attributes) {
				AttributeDO attributeView = new AttributeDO(snAttributeType
						.getName(), snAttributeType.getType(), snAttributeType
						.isMultivalued(), attribute.getAttributeIndex(),
						humanReadableName, description, snAttributeType
								.isUserEditable(), false, attribute
								.getStringValue(), attribute.getBooleanValue());
				snList.add(attributeView);
			}
		}
		return snList;
	}

	private AttributeTypeDescriptionEntity findAttributeTypeDescription(
			AttributeTypeEntity attributeType, Locale locale) {
		String language;
		if (null == locale) {
			language = null;
		} else {
			language = locale.getLanguage();
		}
		if (null != language) {
			AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
					.findDescription(new AttributeTypeDescriptionPK(
							attributeType.getName(), language));
			return attributeTypeDescription;
		}
		return null;
	}

	private NameIdentifierMappingClient getIDMappingClient() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String location = externalContext.getInitParameter("StsWsLocation");
		PrivateKeyEntry privateKeyEntry = DigipassKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate certificate = (X509Certificate) privateKeyEntry
				.getCertificate();

		NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl(
				location, certificate, privateKeyEntry.getPrivateKey());
		return client;
	}
}
