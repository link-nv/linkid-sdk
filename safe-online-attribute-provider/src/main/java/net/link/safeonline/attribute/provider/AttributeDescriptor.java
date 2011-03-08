/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import java.io.Serializable;


/**
 * <h2>{@link AttributeDescriptor}</h2>
 *
 * <p> <i>Nov 29, 2010</i>
 *
 * Abstract Base Attribute class holding attributeId</p>
 *
 * @author wvdhaute
 */
public final class AttributeDescriptor<T extends Serializable> implements Serializable {

    private final Class<T> type;
    private final String attributeName;

    public AttributeDescriptor(Class<T> type, String attributeName) {

        this.attributeName = attributeName;
        this.type = type;

        // TODO: check type against allowed "DataTypes"
    }

    public Class<T> getType() {
        return type;
    }

    public String getAttributeName() {
        return attributeName;
    }
}

