/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.backend;

import java.net.MalformedURLException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.entity.SubjectEntity;

@Local
public interface MobileManager {

	String requestOTP(String mobile) throws MalformedURLException,
			MobileException;

	boolean verifyOTP(String challengeId, String OTPValue)
			throws MalformedURLException, MobileException;

	String activate(String mobile, SubjectEntity subject)
			throws MalformedURLException, MobileException;

	void remove(String mobile) throws MalformedURLException, MobileException;

	String getClientDownloadLink();
}
