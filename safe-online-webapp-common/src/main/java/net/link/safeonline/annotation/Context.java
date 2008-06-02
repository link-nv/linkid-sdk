/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the field should be resolved from the servlet context
 * parameter list.
 * 
 * @author wvdhaute
 * 
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Context {

	public String NOT_SPECIFIED = "Not Specified";

	/**
	 * The name of the context parameter.
	 * 
	 */
	String name();

	/**
	 * Optional default value if missing.
	 */
	String defaultValue() default NOT_SPECIFIED;
}
