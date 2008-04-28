/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.encap;

import java.net.MalformedURLException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface EncapDeviceService {

	String authenticate(String mobile, String challengeId, String mobileOTP)
			throws MalformedURLException, SubjectNotFoundException,
			MobileAuthenticationException, MobileException;

	String register(String deviceUserId, String mobile) throws MobileException,
			MalformedURLException, MobileRegistrationException,
			ArgumentIntegrityException;

	void update(SubjectEntity subject, String oldMobile, String newMobile);

	void remove(String deviceUserId, String mobile) throws MobileException,
			MalformedURLException, SubjectNotFoundException;

	String requestOTP(String mobile) throws MalformedURLException,
			MobileException, SubjectNotFoundException;

	List<String> getMobiles(String login);
}
