/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.ejb.Local;

import net.link.safeonline.entity.SubjectEntity;

import org.apache.axis.AxisFault;

@Local
public interface MobileManager {

	String requestOTP(String mobile) throws MalformedURLException,
			RemoteException, AxisFault;

	boolean verifyOTP(String challengeId, String OTPValue) throws AxisFault,
			MalformedURLException, RemoteException;

	boolean activate(String mobile, SubjectEntity subject)
			throws RemoteException, MalformedURLException;

	void remove(String mobile) throws RemoteException, MalformedURLException;
}
