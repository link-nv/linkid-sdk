/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryObjectInvocationHandler implements InvocationHandler {

	private final EntityManager entityManager;

	public QueryObjectInvocationHandler(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private static final Log LOG = LogFactory
			.getLog(QueryObjectInvocationHandler.class);

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		LOG.debug("invoke: " + method.getName());
		QueryMethod queryMethodAnnotation = method
				.getAnnotation(QueryMethod.class);
		if (null == queryMethodAnnotation) {
			throw new RuntimeException("@QueryMethod annotation expected: "
					+ method.getDeclaringClass().getName());
		}
		String namedQueryName = queryMethodAnnotation.value();
		LOG.debug("named query name: " + namedQueryName);
		Query query = this.entityManager.createNamedQuery(namedQueryName);

		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		if (null != args) {
			for (int paramIdx = 0; paramIdx < args.length; paramIdx++) {
				for (Annotation parameterAnnotation : parameterAnnotations[paramIdx]) {
					if (parameterAnnotation instanceof QueryParam) {
						QueryParam queryParamAnnotation = (QueryParam) parameterAnnotation;
						String paramName = queryParamAnnotation.value();
						query.setParameter(paramName, args[paramIdx]);
					}
				}
			}
		}

		if (List.class.isAssignableFrom(method.getReturnType())) {
			List resultList = query.getResultList();
			return resultList;
		}

		boolean nullable = queryMethodAnnotation.nullable();
		if (true == nullable) {
			List resultList = query.getResultList();
			if (resultList.isEmpty()) {
				return null;
			}
			return resultList.get(0);
		}

		Object singleResult = query.getSingleResult();
		return singleResult;
	}
}
