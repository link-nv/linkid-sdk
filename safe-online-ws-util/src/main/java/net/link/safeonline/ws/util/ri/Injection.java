/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.util.ri;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sun.xml.ws.api.server.InstanceResolverAnnotation;


/**
 * EJB Injection Annotation. To be used on JAX-WS endpoints. This annotation makes it possible to use the EJB
 * annotations on JAX-WS endpoints. Only works for JAX-WS RI 2.1.
 *
 * @author fcorneli
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@InstanceResolverAnnotation(InjectionInstanceResolver.class)
public @interface Injection {

}
