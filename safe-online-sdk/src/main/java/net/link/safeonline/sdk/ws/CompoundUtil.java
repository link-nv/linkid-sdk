/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.lang.reflect.Method;

import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;


/**
 * Utility class for compounded attributes.
 * 
 * @author fcorneli
 * 
 */
public class CompoundUtil {

    private CompoundUtil() {

        // empty
    }

    /**
     * Gives back <code>true</code> if the given value represents a compounded object.
     * 
     * @param attributeValue
     */
    @SuppressWarnings("unchecked")
    public static boolean isCompound(Object attributeValue) {

        if (null == attributeValue) {
            return false;
        }
        Class attributeClass = attributeValue.getClass();
        Compound compoundAnnotation = (Compound) attributeClass.getAnnotation(Compound.class);
        return null != compoundAnnotation;
    }

    /**
     * Gives back the attribute Id of given the compounded object.
     * 
     * @param attributeValue
     */
    public static String getAttributeId(Object attributeValue) {

        Class<?> clazz = attributeValue.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            CompoundId compoundIdAnnotation = method.getAnnotation(CompoundId.class);
            if (null == compoundIdAnnotation) {
                continue;
            }
            if (false == String.class.equals(method.getReturnType())) {
                throw new RuntimeException("method " + method.getName() + " should return a String");
            }
            String attributeId;
            try {
                attributeId = (String) method.invoke(attributeValue, new Object[] {});
            } catch (Exception e) {
                throw new RuntimeException("error invoking @CompoundId method: " + e.getMessage(), e);
            }
            return attributeId;
        }
        throw new IllegalArgumentException("no @CompoundId property found");
    }

    /**
     * Gives back the setter method that corresponds with the given getter for a certain class.
     * 
     * @param clazz
     * @param getMethod
     */
    public static Method getSetMethod(Class<?> clazz, Method getMethod) {

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
            setMethod = clazz.getMethod("set" + propertyName, new Class[] { getMethod.getReturnType() });
        } catch (SecurityException e) {
            throw new RuntimeException("security error: " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("type mismatch for compound member: " + propertyName);
        }
        return setMethod;
    }
}
