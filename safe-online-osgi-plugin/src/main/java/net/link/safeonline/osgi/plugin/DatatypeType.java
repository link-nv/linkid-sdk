/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.osgi.plugin;

/**
 * <h2>{@link Attribute}<br>
 * <sub>Attibute Data types.</sub></h2>
 * 
 * <p>
 * Attribute Data types. Representing the available attribute data types. Used
 * by {@link Attribute}.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public enum DatatypeType {

	STRING("string", true), BOOLEAN("boolean", true), INTEGER("integer", true), DOUBLE(
			"double", true), DATE("date", true), COMPOUNDED;

	private final String friendlyName;

	private final boolean primitive;

	private DatatypeType() {

		this.friendlyName = this.name();
		this.primitive = false;
	}

	private DatatypeType(String friendlyName, boolean primitive) {

		this.friendlyName = friendlyName;
		this.primitive = primitive;
	}

	public String getFriendlyName() {

		return this.friendlyName;
	}

	public boolean isPrimitive() {

		return this.primitive;
	}
}
