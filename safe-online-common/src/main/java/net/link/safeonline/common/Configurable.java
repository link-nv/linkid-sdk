package net.link.safeonline.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

	public static final String defaultGroup = "overall";

	String group() default defaultGroup;

	String name() default "";

}
