/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jpa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.persistence.EntityManager;

public class QueryObjectFactory {

	private QueryObjectFactory() {
		// empty
	}

	@SuppressWarnings("unchecked")
	public static <T> T createQueryObject(EntityManager entityManager,
			Class<T> queryObjectInterface) {
		if (false == queryObjectInterface.isInterface()) {
			throw new IllegalArgumentException(
					"query object class is not an interface");
		}
		Thread currentThread = Thread.currentThread();
		ClassLoader classLoader = currentThread.getContextClassLoader();
		InvocationHandler invocationHandler = new QueryObjectInvocationHandler(
				entityManager);
		T queryObject = (T) Proxy.newProxyInstance(classLoader,
				new Class[] { queryObjectInterface }, invocationHandler);
		return queryObject;
	}
}
