/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.administration.ws;

import http.BankIdAdministration;
import http.BankIdAdministrationService;
import http.BankIdAdministrationServiceLocator;

import javax.xml.rpc.ServiceException;

/**
 * Factory for SafeOnlineAuthenticationService.
 * 
 * @author wvdhaute
 * 
 */
public class SafeOnlineEncapAdministrationServiceFactory {

	private SafeOnlineEncapAdministrationServiceFactory() {
		// empty
	}

	/**
	 * Creates a new instance of the SafeOnline authentication AXIS service
	 * stub.
	 * 
	 * @return a new instance.
	 */
	public static BankIdAdministration newInstance() {
		BankIdAdministrationService serviceLocator;
		try {
			serviceLocator = new BankIdAdministrationServiceLocator();
			return serviceLocator.getmSecBankIdAdministration();
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage());
		}

	}
}
