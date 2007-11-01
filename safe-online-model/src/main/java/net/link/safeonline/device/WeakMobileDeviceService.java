/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.ejb.Local;

import org.apache.axis.AxisFault;

import net.link.safeonline.entity.SubjectEntity;

@Local
public interface WeakMobileDeviceService {

	SubjectEntity authenticate(String login, String challengeId, String OTPValue)
			throws AxisFault, MalformedURLException, RemoteException;

	void register(SubjectEntity subject, String mobile) throws RemoteException,
			MalformedURLException;

	void update(SubjectEntity subject, String oldMobile, String newMobile);

	void remove() throws RemoteException, MalformedURLException;

}
