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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.AccountServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.DeviceMappingDAO;
import net.link.safeonline.dao.DeviceRegistrationDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AccountServiceBean implements AccountService, AccountServiceRemote {

	private static final Log LOG = LogFactory.getLog(AccountServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private DeviceMappingDAO deviceMappingDAO;

	@EJB
	private DeviceRegistrationDAO deviceRegistrationDAO;

	@EJB
	private SubjectService subjectService;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeAccount() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		LOG.debug("remove account: " + subject.getUserId());
		this.historyDAO.clearAllHistory(subject);
		this.subscriptionDAO.removeAllSubscriptions(subject);
		this.attributeDAO.removeAttributes(subject);
		this.subjectIdentifierDAO.removeSubjectIdentifiers(subject);
		this.deviceMappingDAO.removeDeviceMappings(subject);
		removeDeviceRegistrationAttributes(subject);
		this.deviceRegistrationDAO.removeDeviceRegistrations(subject);
		this.subjectDAO.removeSubject(subject);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeAccount(String userId) throws SubjectNotFoundException {
		LOG.debug("remove account: " + userId);
		SubjectEntity subject = this.subjectService.getSubject(userId);
		this.historyDAO.clearAllHistory(subject);
		this.subscriptionDAO.removeAllSubscriptions(subject);
		this.attributeDAO.removeAttributes(subject);
		this.subjectIdentifierDAO.removeSubjectIdentifiers(subject);
		this.deviceMappingDAO.removeDeviceMappings(subject);
		removeDeviceRegistrationAttributes(subject);
		this.deviceRegistrationDAO.removeDeviceRegistrations(subject);
		this.subjectDAO.removeSubject(subject);
	}

	/**
	 * Remove device attributes and possible local device subjects, notify
	 * remote device issuers to do so accordingly
	 * 
	 * @param subject
	 */
	private void removeDeviceRegistrationAttributes(SubjectEntity subject) {
		List<DeviceRegistrationEntity> deviceRegistrations = this.deviceRegistrationDAO
				.listRegisteredDevices(subject);
		for (DeviceRegistrationEntity deviceRegistration : deviceRegistrations) {
			LOG.debug("remove device registration: "
					+ deviceRegistration.getId());
			SubjectEntity deviceSubject = this.subjectService
					.findSubject(deviceRegistration.getId());
			if (null == deviceSubject) {
				// TODO: notify remote device issuers
			} else {
				this.attributeDAO.removeAttributes(deviceSubject);
				this.subjectIdentifierDAO
						.removeSubjectIdentifiers(deviceSubject);
				this.subjectDAO.removeSubject(deviceSubject);
			}
		}

	}
}
