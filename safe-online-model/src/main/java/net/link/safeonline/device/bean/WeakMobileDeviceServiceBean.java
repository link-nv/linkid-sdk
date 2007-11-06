/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.MobileManager;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.device.WeakMobileDeviceServiceRemote;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.service.SubjectService;

@Stateless
public class WeakMobileDeviceServiceBean implements WeakMobileDeviceService,
		WeakMobileDeviceServiceRemote {

	@EJB
	private SubjectService subjectService;

	@EJB
	private MobileManager mobileManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private SecurityAuditLogger securityAuditLogger;

	public SubjectEntity authenticate(String login, String challengeId,
			String mobileOTP) throws MalformedURLException, RemoteException,
			SubjectNotFoundException, MobileRegistrationException {
		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(login);

		boolean result = this.mobileManager.verifyOTP(challengeId, mobileOTP);
		if (false == result) {
			this.historyDAO.addHistoryEntry(new Date(), subject,
					HistoryEventType.LOGIN_INCORRECT_MOBILE_OTP, null, null);
			this.securityAuditLogger.addSecurityAudit(
					SecurityThreatType.DECEPTION, subject.getUserId(),
					"incorrect mobile OTP");
			throw new MobileRegistrationException();
		}
		return subject;
	}

	public void register(SubjectEntity subject, String mobile)
			throws RemoteException, MalformedURLException,
			MobileRegistrationException {
		boolean result = this.mobileManager.activate(mobile, subject);
		if (false == result)
			throw new MobileRegistrationException();
	}

	public void remove() throws RemoteException, MalformedURLException {
		String mobile = "";
		this.mobileManager.remove(mobile);
	}

	public void update(SubjectEntity subject, String oldMobile, String newMobile) {
		// TODO Auto-generated method stub
	}

	public String requestOTP(String mobile) throws MalformedURLException,
			RemoteException {
		return this.mobileManager.requestOTP(mobile);
	}

	// TODO multivalued attribute support
	public List<String> getMobiles(String login) {
		List<String> mobileList = new LinkedList<String>();
		SubjectEntity subject = this.subjectService
				.findSubjectFromUserName(login);
		AttributeEntity weakMobileAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.WEAK_MOBILE_ATTRIBUTE, subject);
		mobileList.add(weakMobileAttribute.getStringValue());
		AttributeEntity strongMobileAttribute = this.attributeDAO
				.findAttribute(SafeOnlineConstants.STRONG_MOBILE_ATTRIBUTE,
						subject);
		mobileList.add(strongMobileAttribute.getStringValue());
		return mobileList;
	}
}
