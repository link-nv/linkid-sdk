/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builder for compound attribute instances. A compound attribute instance in
 * build using the compound annotations.
 * 
 * @author fcorneli
 * 
 */
public class CompoundBuilder {

	private static final Log LOG = LogFactory.getLog(CompoundBuilder.class);

	public static final String ATTRIBUTE_ID_KEY = "@Id";

	private final Class<?> compoundClass;

	private final boolean isMap;

	private final Object compoundAttribute;

	/**
	 * Main constructor. The compound class should be annotated with
	 * {@link Compound} and the member properties with {@link CompoundMember}.
	 * The compound class can also be a simple {@link Map}. In this case the
	 * result map will be filled with name value map entries for every member of
	 * the compounded attribute.
	 * 
	 * @param compoundClass
	 */
	@SuppressWarnings("unchecked")
	public CompoundBuilder(Class compoundClass) {
		this.compoundClass = compoundClass;

		Compound compoundAnnotation = (Compound) compoundClass
				.getAnnotation(Compound.class);
		if (null == compoundAnnotation) {
			if (false == Map.class.isAssignableFrom(compoundClass)) {
				throw new IllegalArgumentException(
						"valueClass not @Compound annotated or not of type java.util.Map");
			}
			this.isMap = true;
			this.compoundAttribute = new HashMap<String, Object>();
		} else {
			this.isMap = false;
			try {
				this.compoundAttribute = compoundClass.newInstance();
			} catch (Exception e) {
				LOG.error("error: " + e.getMessage(), e);
				throw new IllegalArgumentException(
						"could not create new instance for "
								+ compoundClass.getName());
			}
		}
	}

	public Object getCompound() {
		return this.compoundAttribute;
	}

	@SuppressWarnings("unchecked")
	public void setCompoundProperty(String memberName,
			Object memberAttributeValue) {
		if (this.isMap) {
			/*
			 * We also support non-annotated compound results via a simple
			 * java.util.Map.
			 */
			Map<String, Object> compoundMap = (Map<String, Object>) this.compoundAttribute;
			if (compoundMap.containsKey(memberName)) {
				throw new RuntimeException(
						"member already present in result map: " + memberName);
			}
			compoundMap.put(memberName, memberAttributeValue);
			return;
		}
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
			Method setPropertyMethod = CompoundUtil.getSetMethod(
					this.compoundClass, method);
			try {
				setPropertyMethod.invoke(this.compoundAttribute,
						new Object[] { memberAttributeValue });
			} catch (Exception e) {
				throw new RuntimeException("could not invoke: "
						+ setPropertyMethod.getName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setCompoundId(String attributeId) {
		if (this.isMap) {
			/*
			 * We also support non-annotated compound results via a simple
			 * java.util.Map.
			 */
			Map<String, Object> compoundMap = (Map<String, Object>) this.compoundAttribute;
			compoundMap.put(ATTRIBUTE_ID_KEY, attributeId);
			return;
		}
		Method[] methods = this.compoundClass.getMethods();
		for (Method method : methods) {
			CompoundId compoundIdAnnotation = method
					.getAnnotation(CompoundId.class);
			if (null == compoundIdAnnotation) {
				continue;
			}
			Method setPropertyMethod = CompoundUtil.getSetMethod(
					this.compoundClass, method);
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
