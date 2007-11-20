/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
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
	 * Returns the list of devices the user has authenticated successfully with.
	 * 
	 * @return
	 */
	List<AuthenticationDevice> getAuthenticatedDevices();

	/**
	 * Authenticates using a username-password device.
	 * 
	 * @param login
	 * @param password
	 * @return
	 * @throws SubjectNotFoundException
	 * @throws DeviceNotFoundException
	 * @throws SubjectMismatchException
	 */
	boolean authenticate(String login, String password)
			throws SubjectNotFoundException, DeviceNotFoundException,
			SubjectMismatchException;

	/**
	 * Authenticates using a mobile device
	 * 
	 * @param device
	 * @param mobile
	 * @param challengeId
	 * @param mobileOTP
	 * @return
	 * @throws AxisFault
	 * @throws SubjectNotFoundException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws MobileAuthenticationException
	 * @throws SubjectMismatchException
	 */
	String authenticate(AuthenticationDevice device, String mobile,
			String challengeId, String mobileOTP) throws AxisFault,
			SubjectNotFoundException, MalformedURLException, RemoteException,
			MobileAuthenticationException, SubjectMismatchException;

	/**
	 * Requests an OTP for a mobile device. Returns the challenge ID for this
	 * OTP, used for later verification.
	 * 
	 * @param device
	 * @param mobile
	 * @return
	 * @throws MalformedURLException
	 * @throws RemoteException
	 */
	String requestMobileOTP(AuthenticationDevice device, String mobile)
			throws MalformedURLException, RemoteException;

	/**
	 * Authenticates using an authentication statement. The given session Id
	 * must match the one given in the authentication statement. The session Id
	 * is managed by the servlet front-end container.
	 * 
	 * @param sessionId
	 * @param authenticationStatementData
	 * @return
	 * @throws ArgumentIntegrityException
	 * @throws TrustDomainNotFoundException
	 * @throws SubjectNotFoundException
	 * @throws DecodingException
	 * @throws SubjectMismatchException
	 */
	boolean authenticate(String sessionId, byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, DecodingException,
			SubjectMismatchException;

	/**
	 * Aborts the current authentication procedure.
	 */
	void abort();
}