/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.LastDeviceException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.CredentialServiceRemote;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
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
	private PasswordManager passwordController;

	@EJB
	private PasswordDeviceService passwordDeviceService;

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

		this.passwordDeviceService.remove(subject, password);

		SecurityManagerUtils.flushCredentialCache(subject.getUserId(),
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean isPasswordConfigured() {
		SubjectEntity subject = this.subjectManager.getCallerSubject();
		return this.passwordController.isPasswordConfigured(subject);
	}
}
