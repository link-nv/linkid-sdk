/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePolicyException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementAcceptationRequiredException;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;

import org.apache.axis.AxisFault;

/**
 * Authentication service interface. This service allows the authentication web
 * application to authenticate users. The bean behind this interface is
 * stateful. This means that a certain method invocation pattern must be
 * respected. First the methods {@link #authenticate(String, String)} or
 * {@link #authenticate(String, byte[])} must be invoked. Afterwards the
 * {@link #commitAuthentication(String)} method must be invoked. In case the
 * authentication process needs to be aborted one should invoke {@link #abort()}.
 * 
 * @author fcorneli
 */
@Local
public interface AuthenticationService {

	/**
	 * Authenticates a user for a certain application. This method is used by
	 * the authentication web service. If <code>true</code> is returned the
	 * authentication process can proceed, else {@link #abort()} should be
	 * invoked.
	 * 
	 * @param applicationName
	 * @param login
	 * @param password
	 * @return <code>true</code> if the user was authenticated correctly,
	 *         <code>false</code> otherwise.
	 * @throws SubjectNotFoundException
	 * @throws DeviceNotFoundException
	 *             in case the user did not configure the password device.
	 * @throws NoSuchAlgorithmException
	 */
	boolean authenticate(String login, String password)
			throws SubjectNotFoundException, DeviceNotFoundException;

	/**
	 * Authenticates a user for a certain application. This method is used by
	 * the authentication web service. If a non-null username is returned the
	 * authentication process can proceed, else {@link #abort()} should be
	 * invoked.
	 * 
	 * @param device
	 * @param mobile
	 * @param challengeId
	 * @param mobileOTP
	 * @return username
	 * @throws MobileException
	 * @throws MalformedURLException
	 * @throws SubjectNotFoundException
	 * @throws AxisFault
	 * @throws MobileAuthenticationException
	 */
	String authenticate(DeviceEntity device, String mobile, String challengeId,
			String mobileOTP) throws SubjectNotFoundException,
			MalformedURLException, MobileException,
			MobileAuthenticationException;

	/**
	 * Request a OTP be generated for the authenticating users. Returns the
	 * challenge ID for this OTP, used for later verification.
	 * 
	 * @param mobile
	 * @throws MalformedURLException
	 * @throws MobileException
	 */
	String requestMobileOTP(String mobile) throws MalformedURLException,
			MobileException;

	/**
	 * Commits the authentication for the given application.
	 * 
	 * @param applicationId
	 * @param requiredDevicePolicy
	 * @throws SubscriptionNotFoundException
	 *             in case the subject is not subscribed to the application.
	 * @throws ApplicationNotFoundException
	 *             in case the application does not exist.
	 * @throws ApplicationIdentityNotFoundException
	 * @throws IdentityConfirmationRequiredException
	 * @throws MissingAttributeException
	 * @throws EmptyDevicePolicyException
	 * @throws DevicePolicyException
	 * @throws UsageAgreementAcceptationRequiredException
	 */
	void commitAuthentication(String applicationId,
			Set<DeviceEntity> requiredDevicePolicy)
			throws ApplicationNotFoundException, SubscriptionNotFoundException,
			ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException,
			EmptyDevicePolicyException, DevicePolicyException,
			UsageAgreementAcceptationRequiredException;

	/**
	 * Authenticates a user via an authentication statement. The given session
	 * Id must match the one given in the authentication statement. The session
	 * Id is managed by the servlet front-end container.
	 * 
	 * @param sessionId
	 * @param authenticationStatementData
	 * @return <code>true</code> if the authentication process can proceed.
	 * @throws ArgumentIntegrityException
	 * @throws TrustDomainNotFoundException
	 * @throws SubjectNotFoundException
	 *             in case the certificate was not linked to any subject.
	 * @throws DecodingException
	 */
	boolean authenticate(String sessionId, byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, DecodingException;

	/**
	 * Registers and authenticates a new user via a registration statement.
	 * 
	 * @param sessionId
	 * @param username
	 * @param registrationStatementData
	 * @throws ArgumentIntegrityException
	 * @throws TrustDomainNotFoundException
	 * @throws DecodingException
	 * @throws ExistingUserException
	 * @throws NoSuchAlgorithmException
	 * @throws SubjectIdNotUniqueException
	 * @throws AttributeTypeNotFoundException
	 * @throws SubjectNotFoundException
	 */
	boolean registerAndAuthenticate(String sessionId, String username,
			byte[] registrationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			DecodingException, ExistingUserException,
			AttributeTypeNotFoundException;

	/**
	 * Registers a device for a logged in user via an identity statement.
	 * 
	 * @param identityStatementData
	 * @throws TrustDomainNotFoundException
	 * @throws PermissionDeniedException
	 * @throws ArgumentIntegrityException
	 * @throws AttributeTypeNotFoundException
	 */
	boolean registerDevice(byte[] identityStatementData)
			throws TrustDomainNotFoundException, PermissionDeniedException,
			ArgumentIntegrityException, AttributeTypeNotFoundException;

	/**
	 * Registers a mobile for a logged in user. Returns activation code.
	 * 
	 * @param mobile
	 * @throws MobileException
	 * @throws MalformedURLException
	 * @throws MobileRegistrationException
	 * @throws ArgumentIntegrityException
	 */
	String registerMobile(String mobile) throws MobileException,
			MalformedURLException, MobileRegistrationException,
			ArgumentIntegrityException;

	/**
	 * User canceled the mobile activation. Remove just registered mobile.
	 * 
	 * @param mobile
	 * @throws MalformedURLException
	 * @throws MobileException
	 */
	void removeMobile(String mobile) throws MobileException,
			MalformedURLException;

	/**
	 * Sets the password of a user. This method should be used in case the user
	 * did not yet had a password registered as authentication device.
	 * 
	 * @param password
	 * @throws PermissionDeniedException
	 */
	void setPassword(String password) throws PermissionDeniedException;

	/**
	 * Aborts the current authentication procedure.
	 */
	void abort();

	/**
	 * Gives back the user Id of the user that we're trying to authenticate.
	 * Calling this method in only valid after a call to
	 * {@link #authenticate(String, byte[])}.
	 * 
	 */
	String getUserId();

	/**
	 * Gives back the username of the user that we're trying to authenticate.
	 * Calling this method in only valid after a call to
	 * {@link #authenticate(String, byte[])}.
	 * 
	 */
	String getUsername();
}
