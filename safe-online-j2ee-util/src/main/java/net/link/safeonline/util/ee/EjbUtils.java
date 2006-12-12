/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.ee;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utils to ease the working with EJBs.
 * 
 * @author fcorneli
 * 
 */
public class EjbUtils {

	private static final Log LOG = LogFactory.getLog(EjbUtils.class);

	private EjbUtils() {
		// empty
	}

	/**
	 * Lookup an EJB within JNDI.
	 * 
	 * @param <Type>
	 * @param initialContext
	 * @param jndiName
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Type getEJB(InitialContext initialContext,
			String jndiName, Class<Type> type) {
		try {
			LOG.debug("ejb jndi lookup: " + jndiName);
			Object obj = initialContext.lookup(jndiName);
			if (!type.isInstance(obj)) {
				throw new RuntimeException(jndiName + " is not a "
						+ type.getName() + " but a " + obj.getClass().getName());
			}
			Type instance = (Type) obj;
			return instance;
		} catch (NamingException e) {
			throw new RuntimeException("naming error: " + e.getMessage(), e);
		}
	}

	public static <Type> Type getEJB(String jndiName, Class<Type> type) {
		InitialContext initialContext;
		try {
			initialContext = new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException("naming error: " + e.getMessage(), e);
		}
		return getEJB(initialContext, jndiName, type);
	}
}
