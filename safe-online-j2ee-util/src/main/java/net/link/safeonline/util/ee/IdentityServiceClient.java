/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Client for SafeOnline Identity Service JMX bean. This service manages the key
 * pair of the SafeOnline service itself. See also: safe-online-service.
 * 
 * @author fcorneli
 * 
 */
public class IdentityServiceClient {

	private static final Log LOG = LogFactory
			.getLog(IdentityServiceClient.class);

	private final MBeanServer server;

	private final ObjectName identityServiceName;

	public static final String IDENTITY_SERVICE = "safeonline:service=Identity";

	/**
	 * Main constructor.
	 */
	public IdentityServiceClient() {
		ArrayList mbeanServers = MBeanServerFactory.findMBeanServer(null);
		if (mbeanServers.isEmpty()) {
			throw new RuntimeException(
					"no MBean server found; probably not running inside J2EE container");
		}
		this.server = (MBeanServer) mbeanServers.get(0);
		LOG.debug("MBean Server class: " + this.server.getClass().getName());
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
					this.identityServiceName, "getPrivateKey", params,
					signature);
		} catch (Exception e) {
			throw new RuntimeException("invoke error: " + e.getMessage(), e);
		}
		return privateKey;
	}

	/**
	 * Gives back the public key of the SafeOnline service entity.
	 * 
	 * @return
	 */
	public PublicKey getPublicKey() {
		Object[] params = {};
		String[] signature = {};
		PublicKey publicKey;
		try {
			publicKey = (PublicKey) this.server
					.invoke(this.identityServiceName, "getPublicKey", params,
							signature);
		} catch (Exception e) {
			throw new RuntimeException("invoke error: " + e.getMessage(), e);
		}
		return publicKey;
	}
}
