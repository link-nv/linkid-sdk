/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

public enum DatatypeType {

    STRING("string", true),
    BOOLEAN("boolean", true),
    INTEGER("integer", true),
    DOUBLE("double", true),
    DATE("date", true),
    COMPOUNDED;

    private final String  friendlyName;

    private final boolean primitive;


    private DatatypeType() {

        friendlyName = name();
        primitive = false;
    }

    private DatatypeType(String friendlyName, boolean primitive) {

        this.friendlyName = friendlyName;
        this.primitive = primitive;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public boolean isPrimitive() {

        return primitive;
    }
}
