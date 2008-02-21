/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.LastDeviceException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.CredentialServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.device.backend.CredentialManager;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

/**
 * Implementation of the credential service interface.
 * 
 * @author fcorneli
 * 
 */
@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class CredentialServiceBean implements CredentialService,
		CredentialServiceRemote {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private CredentialManager credentialManager;

	@EJB
	private PasswordManager passwordController;

	@EJB
	private PasswordDeviceService passwordDeviceService;

	@EJB
	private WeakMobileDeviceService weakMobileDeviceService;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException, DeviceNotFoundException {
		LOG.debug("change password");
		SubjectEntity subject = this.subjectManager.getCallerSubject();

		this.passwordDeviceService.update(subject, oldPassword, newPassword);

		SecurityManagerUtils.flushCredentialCache(subject.getUserId(),
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removePassword(String password) throws DeviceNotFoundException,
			PermissionDeniedException, LastDeviceException {
		LOG.debug("remove password");
		SubjectEntity subject = this.subjectManager.getCallerSubject();

		if (lastDevice(subject))
			throw new LastDeviceException(
					"At least 1 authentication device needed ...");

		this.passwordDeviceService.remove(subject, password);

		SecurityManagerUtils.flushCredentialCache(subject.getUserId(),
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void mergeIdentityStatement(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("merge identity statement");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		this.credentialManager.mergeIdentityStatement(subject,
				identityStatementData);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeIdentity(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException {
		LOG.debug("remove identity");
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		this.credentialManager.removeIdentity(subject, identityStatementData);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean isPasswordConfigured() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		return this.passwordController.isPasswordConfigured(subject);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String registerMobile(String mobile) throws MobileException,
			MalformedURLException, MobileRegistrationException,
			ArgumentIntegrityException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		return this.weakMobileDeviceService.register(subject.getUserId(),
				mobile);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void removeMobile(String mobile) throws MobileException,
			MalformedURLException, LastDeviceException,
			SubjectNotFoundException {
		SubjectEntity subject = this.subjectManager.getCallerSubject();

		if (lastDevice(subject))
			throw new LastDeviceException(
					"At least 1 authentication device needed ...");
		this.weakMobileDeviceService.remove(subject.getUserId(), mobile);
	}

	private boolean lastDevice(SubjectEntity subject) {
		List<DeviceEntity> devices = this.deviceDAO.listDevices();
		int devicesFound = 0;
		for (DeviceEntity device : devices) {
			List<AttributeTypeEntity> deviceAttributeTypes = device
					.getAttributeTypes();
			for (AttributeTypeEntity deviceAttributeType : deviceAttributeTypes) {
				List<AttributeEntity> deviceAttributes = this.attributeDAO
						.listAttributes(subject, deviceAttributeType);
				if (null != deviceAttributes) {
					devicesFound += deviceAttributes.size();
					break;
				}
			}
		}
		LOG.debug("# devices found: " + devicesFound);
		return (1 == devicesFound);
	}
}
