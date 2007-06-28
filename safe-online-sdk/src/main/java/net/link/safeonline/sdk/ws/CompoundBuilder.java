/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;

/**
 * Builder for compound attribute instances. A compound attribute instance in
 * build using the compound annotations.
 * 
 * @author fcorneli
 * 
 */
public class CompoundBuilder {

	private static final Log LOG = LogFactory.getLog(CompoundBuilder.class);

	private final Class compoundClass;

	private final Object compoundAttribute;

	/**
	 * Main constructor. The compound class should be annotated with
	 * {@link Compound} and the member properties with {@link CompoundMember}.
	 * 
	 * @param compoundClass
	 */
	@SuppressWarnings("unchecked")
	public CompoundBuilder(Class compoundClass) {
		this.compoundClass = compoundClass;

		Compound compoundAnnotation = (Compound) compoundClass
				.getAnnotation(Compound.class);
		if (null == compoundAnnotation) {
			throw new IllegalArgumentException(
					"valueClass not @Compound annotated");
		}
		try {
			this.compoundAttribute = compoundClass.newInstance();
		} catch (Exception e) {
			LOG.error("error: " + e.getMessage(), e);
			throw new IllegalArgumentException(
					"could not create new instance for "
							+ compoundClass.getName());
		}
	}

	public Object getCompound() {
		return this.compoundAttribute;
	}

	private Method getSetMethod(Method getMethod) {
		String methodName = getMethod.getName();
		String propertyName;
		if (methodName.startsWith("get")) {
			propertyName = methodName.substring(3);
		} else if (methodName.startsWith("is")) {
			propertyName = methodName.substring(2);
		} else {
			throw new RuntimeException("not a property: " + methodName);
		}
		Method setMethod;
		try {
			setMethod = this.compoundClass.getMethod("set" + propertyName,
					new Class[] { getMethod.getReturnType() });
		} catch (SecurityException e) {
			throw new RuntimeException("security error: " + e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("type mismatch for compound member: "
					+ propertyName);
		}
		return setMethod;
	}

	public void setCompoundProperty(String memberName,
			Object memberAttributeValue) {
		Method[] methods = this.compoundClass.getMethods();
		for (Method method : methods) {
			CompoundMember compoundMemberAnnotation = method
					.getAnnotation(CompoundMember.class);
			if (null == compoundMemberAnnotation) {
				continue;
			}
			if (false == memberName.equals(compoundMemberAnnotation.value())) {
				continue;
			}
			Method setPropertyMethod = getSetMethod(method);
			try {
				setPropertyMethod.invoke(this.compoundAttribute,
						new Object[] { memberAttributeValue });
			} catch (Exception e) {
				throw new RuntimeException("could not invoke: "
						+ setPropertyMethod.getName());
			}
		}
	}

	public void setCompoundId(String attributeId) {
		Method[] methods = this.compoundClass.getMethods();
		for (Method method : methods) {
			CompoundId compoundIdAnnotation = method
					.getAnnotation(CompoundId.class);
			if (null == compoundIdAnnotation) {
				continue;
			}
			Method setPropertyMethod = getSetMethod(method);
			try {
				setPropertyMethod.invoke(this.compoundAttribute,
						new Object[] { attributeId });
			} catch (Exception e) {
				throw new RuntimeException("could not invoke: "
						+ setPropertyMethod.getName());
			}
		}
	}
}
