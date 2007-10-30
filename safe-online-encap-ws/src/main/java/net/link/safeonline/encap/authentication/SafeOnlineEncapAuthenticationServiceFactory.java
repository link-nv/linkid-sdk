/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.authentication;

import javax.xml.rpc.ServiceException;

import _0._1.authentication.encap.safe_online.link.net.BankIdAuthentication;
import _0._1.authentication.encap.safe_online.link.net.BankIdAuthenticationService;
import _0._1.authentication.encap.safe_online.link.net.BankIdAuthenticationServiceLocator;

/**
 * Factory for SafeOnlineAuthenticationService.
 * 
 * @author wvdhaute
 * 
 */
public class SafeOnlineEncapAuthenticationServiceFactory {

	private SafeOnlineEncapAuthenticationServiceFactory() {
		// empty
	}

	/**
	 * Creates a new instance of the SafeOnline authentication AXIS service
	 * stub.
	 * 
	 * @return a new instance.
	 */
	public static BankIdAuthentication newInstance() {
		BankIdAuthenticationService serviceLocator;
		try {
			serviceLocator = new BankIdAuthenticationServiceLocator();
			return serviceLocator.getmSecBankId();
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage());
		}

	}
}
