/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.axis.AxisFault;

@Local
public interface WeakMobileDeviceService {

	SubjectEntity authenticate(String mobile, String challengeId,
			String mobileOTP) throws AxisFault, MalformedURLException,
			RemoteException, SubjectNotFoundException,
			MobileAuthenticationException;

	String register(SubjectEntity subject, String mobile)
			throws RemoteException, MalformedURLException,
			MobileRegistrationException, ArgumentIntegrityException;

	void update(SubjectEntity subject, String oldMobile, String newMobile);

	void remove(SubjectEntity subject, String mobile) throws RemoteException,
			MalformedURLException;

	String requestOTP(String mobile) throws MalformedURLException,
			RemoteException;

	List<String> getMobiles(String login);
}
