/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.bean;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.MobileManager;
import net.link.safeonline.device.WeakMobileDeviceService;
import net.link.safeonline.device.WeakMobileDeviceServiceRemote;
import net.link.safeonline.entity.SubjectEntity;

@Stateless
public class WeakMobileDeviceServiceBean implements WeakMobileDeviceService,
		WeakMobileDeviceServiceRemote {

	@EJB
	MobileManager mobileManager;

	public SubjectEntity authenticate(String login, String challengeId,
			String OTPValue) throws MalformedURLException, RemoteException {
		boolean result = this.mobileManager.verifyOTP(challengeId, OTPValue);
		if (false == result)
			return null;
		return null;
	}

	public void register(SubjectEntity subject, String mobile)
			throws RemoteException, MalformedURLException {
		this.mobileManager.activate(mobile, subject);
	}

	public void remove() throws RemoteException, MalformedURLException {
		String mobile = "";
		this.mobileManager.remove(mobile);
	}

	public void update(SubjectEntity subject, String oldMobile, String newMobile) {
		// TODO Auto-generated method stub
	}

}
