/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.common;

/**
 * <h2>{@link NamingStrategy}<br>
 * <sub>Strategy which resolves the JNDI name of the given type.</sub></h2>
 * 
 * <p>
 * <i>Mar 3, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public interface NamingStrategy {

    public String calculateName(Class<?> ejbType);
}
