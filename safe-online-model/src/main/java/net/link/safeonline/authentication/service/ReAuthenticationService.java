/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.net.MalformedURLException;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.apache.axis.AxisFault;

/**
 * Re-authentication service used by the user web application for account
 * merging. This service lets a logged in user authenticate with the available
 * devices for another account. It stores all the authentication devices that
 * have been authenticated with.
 * 
 * The implementation is stateful so explicit cleanup is required. Use
 * {@link #cleanup()} to reset all authentication devices.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface ReAuthenticationService {

	/**
	 * Returns the set of devices the user has authenticated successfully with.
	 * 
	 */
	Set<AuthenticationDevice> getAuthenticatedDevices();

	/**
	 * Sets the source subject.
	 * 
	 * @param subject
	 * @throws SubjectMismatchException
	 * @throws PermissionDeniedException
	 */
	void setAuthenticatedSubject(SubjectEntity subject)
			throws SubjectMismatchException, PermissionDeniedException;

	/**
	 * Authenticates using a username-password device.
	 * 
	 * @param login
	 * @param password
	 * @throws SubjectNotFoundException
	 * @throws DeviceNotFoundException
	 * @throws SubjectMismatchException
	 * @throws PermissionDeniedException
	 */
	boolean authenticate(String login, String password)
			throws SubjectNotFoundException, DeviceNotFoundException,
			SubjectMismatchException, PermissionDeniedException;

	/**
	 * Authenticates using a mobile device
	 * 
	 * @param device
	 * @param mobile
	 * @param challengeId
	 * @param mobileOTP
	 * @throws AxisFault
	 * @throws SubjectNotFoundException
	 * @throws MalformedURLException
	 * @throws MobileException
	 * @throws MobileAuthenticationException
	 * @throws SubjectMismatchException
	 * @throws PermissionDeniedException
	 */
	String authenticate(AuthenticationDevice device, String mobile,
			String challengeId, String mobileOTP)
			throws SubjectNotFoundException, MalformedURLException,
			MobileException, MobileAuthenticationException,
			SubjectMismatchException, PermissionDeniedException;

	/**
	 * Requests an OTP for a mobile device. Returns the challenge ID for this
	 * OTP, used for later verification.
	 * 
	 * @param device
	 * @param mobile
	 * @throws MalformedURLException
	 * @throws MobileException
	 */
	String requestMobileOTP(AuthenticationDevice device, String mobile)
			throws MalformedURLException, MobileException;

	/**
	 * Authenticates using an authentication statement. The given session Id
	 * must match the one given in the authentication statement. The session Id
	 * is managed by the servlet front-end container.
	 * 
	 * @param sessionId
	 * @param authenticationStatementData
	 * @throws ArgumentIntegrityException
	 * @throws TrustDomainNotFoundException
	 * @throws SubjectNotFoundException
	 * @throws DecodingException
	 * @throws SubjectMismatchException
	 * @throws PermissionDeniedException
	 */
	boolean authenticate(String sessionId, byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, DecodingException,
			SubjectMismatchException, PermissionDeniedException;

	/**
	 * Aborts the current authentication procedure.
	 */
	void abort();
}
