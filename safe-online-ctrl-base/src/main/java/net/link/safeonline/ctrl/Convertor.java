/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl;

/**
 * Interface for convertor components.
 * 
 * @author fcorneli
 * 
 * @param <TypeIn>
 * @param <TypeOut>
 */
public interface Convertor<TypeIn, TypeOut> {
	TypeOut convert(TypeIn input);
}