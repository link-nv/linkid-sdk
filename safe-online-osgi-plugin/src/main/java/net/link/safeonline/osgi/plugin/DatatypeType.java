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
 * Attribute Data types. Representing the available attribute data types. Used by {@link Attribute}. The primitive property is to make a
 * distinction between a compound attribute and primitive attributes.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public enum DatatypeType {

    STRING("string", true),
    BOOLEAN("boolean", true),
    INTEGER("integer", true),
    DOUBLE("double", true),
    DATE("date", true),
    COMPOUNDED;

    private final String  friendlyName;

    private final boolean primitive;


    /**
     * Constructor
     */
    private DatatypeType() {

        this.friendlyName = this.name();
        this.primitive = false;
    }

    /**
     * Constructor
     * 
     * @param friendlyName
     * @param primitive
     */
    private DatatypeType(String friendlyName, boolean primitive) {

        this.friendlyName = friendlyName;
        this.primitive = primitive;
    }

    /**
     * Returns the data type's friendly name.
     * 
     * @return friendly name
     */
    public String getFriendlyName() {

        return this.friendlyName;
    }

    /**
     * Returns whether this data type is primitive or compounded
     * 
     * @return true if primitive
     */
    public boolean isPrimitive() {

        return this.primitive;
    }
}
