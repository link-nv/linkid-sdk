/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.activation.ws;

import http.BankIdActivation;
import http.BankIdActivationService;
import http.BankIdActivationServiceLocator;

import javax.xml.rpc.ServiceException;

/**
 * Factory for SafeOnlineAuthenticationService.
 * 
 * @author wvdhaute
 * 
 */
public class SafeOnlineEncapActivationServiceFactory {

	private SafeOnlineEncapActivationServiceFactory() {
		// empty
	}

	/**
	 * Creates a new instance of the SafeOnline authentication AXIS service
	 * stub.
	 * 
	 * @return a new instance.
	 */
	public static BankIdActivation newInstance() {
		BankIdActivationService serviceLocator;
		try {
			serviceLocator = new BankIdActivationServiceLocator();
			return serviceLocator.getmSecBankIdActivation();
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage());
		}

	}
}
