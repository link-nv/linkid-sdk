/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * <h2>{@link AttributeSDK}</h2>
 *
 * <p> <i>Nov 29, 2010</i>
 *
 * SDK Attribute.</p>
 *
 * @author wvdhaute
 */
public class AttributeSDK<T extends Serializable> extends AttributeAbstract<T> {

    private final String attributeName;

    public AttributeSDK(final String attributeName) {
        super( null );
        this.attributeName = attributeName;
    }

    public AttributeSDK(final String attributeId, final String attributeName) {
        super( attributeId );
        this.attributeName = attributeName;
    }

    public AttributeSDK(final String attributeName, final T value) {
        super( null );
        this.attributeName = attributeName;
        this.value = value;
    }

    public AttributeSDK(final String attributeId, final String attributeName, final T value) {
        super( attributeId );
        this.attributeName = attributeName;
        this.value = value;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof AttributeSDK))
            return false;
        AttributeSDK<T> rhs = (AttributeSDK<T>) obj;
        return new EqualsBuilder().append( attributeName, rhs.attributeName )
                .append( attributeId, rhs.attributeId )
                .append( value, rhs.value )
                .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append( attributeName ).append( attributeId ).append( value ).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder( this ).append( "attributeName", attributeName )
                .append( "attributeId", attributeId )
                .append( "value", value )
                .toString();
    }
}

