/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import java.security.PrivateKey;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Client for SafeOnline Identity Service JMX bean. This service manages the key
 * pair of the SafeOnline service itself. See also: safe-online-service.
 * 
 * @author fcorneli
 * 
 */
public class IdentityServiceClient {

	private final MBeanServer server;

	private final ObjectName identityServiceName;

	public static final String IDENTITY_SERVICE = "safeonline:service=Identity";

	/**
	 * Main constructor.
	 */
	public IdentityServiceClient() {
		this.server = (MBeanServer) MBeanServerFactory.findMBeanServer(null)
				.get(0);
		try {
			this.identityServiceName = new ObjectName(IDENTITY_SERVICE);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("object name error: " + e.getMessage());
		}
	}

	/**
	 * Gives back the private key of the SafeOnline service entity.
	 * 
	 * @return
	 */
	public PrivateKey getPrivateKey() {
		Object[] params = {};
		String[] signature = {};
		PrivateKey privateKey;
		try {
			privateKey = (PrivateKey) this.server.invoke(
					this.identityServiceName, "loadPrivateKey", params,
					signature);
		} catch (Exception e) {
			throw new RuntimeException("invoke error: " + e.getMessage(), e);
		}
		return privateKey;
	}
}
