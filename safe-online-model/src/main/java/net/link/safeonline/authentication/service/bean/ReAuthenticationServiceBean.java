/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.AccessAuditLogger;
import net.link.safeonline.audit.AuditContextManager;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.device.BeIdDeviceService;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.StrongMobileDeviceService;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.validation.InputValidation;
import net.link.safeonline.validation.annotation.NonEmptyString;
import net.link.safeonline.validation.annotation.NotNull;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@Stateful
@Interceptors( { AuditContextManager.class, AccessAuditLogger.class,
		InputValidation.class })
public class ReAuthenticationServiceBean implements ReAuthenticationService {

	private final static Log LOG = LogFactory
			.getLog(ReAuthenticationServiceBean.class);

	private SubjectEntity authenticatedSubject;

	private List<AuthenticationDevice> authenticationDevices;

	@EJB
	private SubjectService subjectService;

	@EJB
	private PasswordDeviceService passwordDeviceService;

	@EJB
	private BeIdDeviceService beIdDeviceService;

	@EJB
	private WeakMobileDeviceService weakMobileDeviceService;

	@EJB
	private StrongMobileDeviceService strongMobileDeviceService;

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("PostConstruct");
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<AuthenticationDevice> getAuthenticatedDevices() {
		return this.authenticationDevices;
	}

	private void addAuthenticationDevice(
			AuthenticationDevice authenticationDevice) {
		if (null == this.authenticationDevices)
			this.authenticationDevices = new LinkedList<AuthenticationDevice>();
		this.authenticationDevices.add(authenticationDevice);
	}

	/**
	 * Checks, if already re-authenticated, if we're dealing with the same
	 * subject.
	 * 
	 * @param subject
	 * @throws SubjectMismatchException
	 */
	private void setAuthenticatedSubject(SubjectEntity subject)
			throws SubjectMismatchException {
		if (null == this.authenticatedSubject) {
			this.authenticatedSubject = subject;
			return;
		}
		if (this.authenticatedSubject != subject)
			throw new SubjectMismatchException();
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean authenticate(@NonEmptyString
	String login, @NonEmptyString
	String password) throws SubjectNotFoundException, DeviceNotFoundException,
			SubjectMismatchException {
		SubjectEntity subject = this.passwordDeviceService.authenticate(login,
				password);
		if (null == subject)
			return false;

		/*
		 * Safe the state in this stateful session bean.
		 */
		setAuthenticatedSubject(subject);
		addAuthenticationDevice(AuthenticationDevice.PASSWORD);

		/*
		 * Communicate that the authentication process can continue.
		 */
		return true;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String authenticate(@NotNull
	AuthenticationDevice device, @NonEmptyString
	String mobile, @NonEmptyString
	String challengeId, @NonEmptyString
	String mobileOTP) throws AxisFault, SubjectNotFoundException,
			MalformedURLException, RemoteException,
			MobileAuthenticationException, SubjectMismatchException {
		SubjectEntity subject;
		if (device == AuthenticationDevice.WEAK_MOBILE)
			subject = this.weakMobileDeviceService.authenticate(mobile,
					challengeId, mobileOTP);
		else if (device == AuthenticationDevice.STRONG_MOBILE)
			subject = this.strongMobileDeviceService.authenticate(mobile,
					challengeId, mobileOTP);
		else
			return null;
		/*
		 * Safe the state in this stateful session bean.
		 */
		setAuthenticatedSubject(subject);
		addAuthenticationDevice(device);

		/*
		 * Communicate that the authentication process can continue.
		 */
		return this.subjectService.getSubjectLogin(subject.getUserId());
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public String requestMobileOTP(@NotNull
	AuthenticationDevice device, @NonEmptyString
	String mobile) throws MalformedURLException, RemoteException {
		if (device == AuthenticationDevice.WEAK_MOBILE)
			return this.weakMobileDeviceService.requestOTP(mobile);
		else if (device == AuthenticationDevice.STRONG_MOBILE)
			return this.strongMobileDeviceService.requestOTP(mobile);
		return null;
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public boolean authenticate(@NonEmptyString
	String sessionId, @NotNull
	byte[] authenticationStatementData) throws ArgumentIntegrityException,
			TrustDomainNotFoundException, SubjectNotFoundException,
			DecodingException, SubjectMismatchException {
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				authenticationStatementData);
		SubjectEntity subject = this.beIdDeviceService.authenticate(sessionId,
				authenticationStatement);
		if (null == subject)
			return false;

		/*
		 * Safe the state.
		 */
		setAuthenticatedSubject(subject);
		addAuthenticationDevice(AuthenticationDevice.BEID);

		return true;
	}

	@Remove
	public void abort() {
		LOG.debug("abort");
		this.authenticatedSubject = null;
		this.authenticationDevices = null;
	}

}
