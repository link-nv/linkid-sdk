/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.authentication;

import http.BankIdAuthentication;
import http.BankIdAuthenticationService;
import http.BankIdAuthenticationServiceLocator;

import javax.xml.rpc.ServiceException;

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
