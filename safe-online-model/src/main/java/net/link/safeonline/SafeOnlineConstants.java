/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline;

/**
 * Defines various SafeOnline constants.
 * 
 * @author fcorneli
 * 
 */
public class SafeOnlineConstants {

	/**
	 * The SafeOnline JPA entity manager unit name.
	 */
	public static final String SAFE_ONLINE_ENTITY_MANAGER = "SafeOnlineEntityManager";

	/**
	 * The JBoss AS security domain for the SafeOnline components that need to
	 * be accessible by users.
	 */
	public static final String SAFE_ONLINE_SECURITY_DOMAIN = "safe-online";

	/**
	 * The JBoss AS security domain for the SafeOnline components that need to
	 * be accessible by applications.
	 */
	public static final String SAFE_ONLINE_APPLICATION_SECURITY_DOMAIN = "safe-online-application";

	/**
	 * The PKI trust domain name for the SafeOnline application owner
	 * applications.
	 */
	public static final String SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN = "applications";

	public static final String SAFE_ONLINE_USER_APPLICATION_NAME = "safe-online-user";

	public static final String SAFE_ONLINE_OPERATOR_APPLICATION_NAME = "safe-online-oper";

	public static final String SAFE_ONLINE_OWNER_APPLICATION_NAME = "safe-online-owner";

	public static final String NAME_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:name";

	public static final String PASSWORD_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:password";

	private SafeOnlineConstants() {
		// empty
	}
}
