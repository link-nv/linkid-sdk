/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import java.io.Serializable;


/**
 * <h2>{@link AttributeAbstract}</h2>
 *
 * <p> <i>Nov 29, 2010</i>
 *
 * Abstract Base Attribute class holding attributeId</p>
 *
 * @author wvdhaute
 */
public abstract class AttributeAbstract<T extends Serializable> implements Serializable {

    protected String attributeId;

    protected T value;

    protected AttributeAbstract(final String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public abstract String getAttributeName();
}

