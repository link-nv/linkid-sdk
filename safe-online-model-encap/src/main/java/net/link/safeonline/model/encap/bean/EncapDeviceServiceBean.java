/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap.bean;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.device.backend.MobileManager;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.model.encap.EncapDeviceServiceRemote;
import net.link.safeonline.service.SubjectService;

@Stateless
public class EncapDeviceServiceBean implements EncapDeviceService,
		EncapDeviceServiceRemote {

	@EJB
	private SubjectService subjectService;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private MobileManager mobileManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	public String authenticate(String mobile, String challengeId,
			String mobileOTP) throws MalformedURLException, MobileException,
			SubjectNotFoundException, MobileAuthenticationException {
		SubjectEntity deviceRegistration = this.subjectIdentifierDAO
				.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
		if (null == deviceRegistration)
			throw new SubjectNotFoundException();
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceRegistration);

		boolean result = this.mobileManager.verifyOTP(challengeId, mobileOTP);
		if (false == result) {
			this.historyDAO.addHistoryEntry(new Date(), deviceRegistration,
					HistoryEventType.LOGIN_INCORRECT_MOBILE_TOKEN, null, null);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, deviceSubject.getId(),
					"incorrect mobile token");
			throw new MobileAuthenticationException();
		}
		return deviceSubject.getId();
	}

	public String register(String deviceUserId, String mobile)
			throws MobileException, MalformedURLException,
			MobileRegistrationException, ArgumentIntegrityException {

		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
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

		String activationCode = this.mobileManager.activate(mobile,
				deviceRegistration);
		if (null == activationCode)
			throw new MobileRegistrationException();
		setMobile(deviceRegistration, mobile);

		this.subjectIdentifierDAO.addSubjectIdentifier(
				EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile,
				deviceRegistration);
		return activationCode;
	}

	private void setMobile(SubjectEntity subject, String mobile) {
		AttributeTypeEntity mobileAttributeType;
		try {
			mobileAttributeType = this.attributeTypeDAO
					.getAttributeType(EncapConstants.MOBILE_ENCAP_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("weak mobile attribute type not found");
		}
		List<AttributeEntity> mobileAttributes = this.attributeDAO
				.listAttributes(subject, mobileAttributeType);
		AttributeEntity mobileAttribute = this.attributeDAO.addAttribute(
				mobileAttributeType, subject, mobileAttributes.size());
		mobileAttribute.setStringValue(mobile);
	}

	public void remove(String deviceUserId, String mobile)
			throws MobileException, MalformedURLException,
			SubjectNotFoundException {
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);
		SubjectEntity deviceRegistration = this.subjectIdentifierDAO
				.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
		if (null == deviceRegistration)
			throw new MobileException("device registration not found");

		AttributeTypeEntity mobileAttributeType;
		try {
			mobileAttributeType = this.attributeTypeDAO
					.getAttributeType(EncapConstants.MOBILE_ENCAP_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("weak mobile attribute type not found");
		}
		this.subjectIdentifierDAO.removeSubjectIdentifier(deviceRegistration,
				EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
		List<AttributeEntity> mobileAttributes = this.attributeDAO
				.listAttributes(deviceRegistration, mobileAttributeType);
		for (AttributeEntity mobileAttribute : mobileAttributes) {
			if (mobileAttribute.getStringValue().equals(mobile))
				this.attributeDAO.removeAttribute(mobileAttribute);
		}
		this.mobileManager.remove(mobile);
		deviceSubject.getRegistrations().remove(deviceRegistration);
	}

	public void update(SubjectEntity subject, String oldMobile, String newMobile) {
		// TODO Auto-generated method stub
	}

	public String requestOTP(String mobile) throws MalformedURLException,
			MobileException, SubjectNotFoundException {
		SubjectEntity deviceRegistration = this.subjectIdentifierDAO
				.findSubject(EncapConstants.ENCAP_IDENTIFIER_DOMAIN, mobile);
		if (null == deviceRegistration)
			throw new SubjectNotFoundException();
		return this.mobileManager.requestOTP(mobile);
	}

	public List<AttributeEntity> getMobiles(String deviceUserId)
			throws SubjectNotFoundException {
		AttributeTypeEntity mobileAttributeType;
		try {
			mobileAttributeType = this.attributeTypeDAO
					.getAttributeType(EncapConstants.MOBILE_ENCAP_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("weak mobile attribute type not found");
		}
		List<AttributeEntity> mobileList = new LinkedList<AttributeEntity>();
		DeviceSubjectEntity deviceSubject = this.subjectService
				.getDeviceSubject(deviceUserId);
		for (SubjectEntity deviceRegistration : deviceSubject
				.getRegistrations()) {
			mobileList.addAll(this.attributeDAO.listAttributes(
					deviceRegistration, mobileAttributeType));
		}
		return mobileList;
	}
}
