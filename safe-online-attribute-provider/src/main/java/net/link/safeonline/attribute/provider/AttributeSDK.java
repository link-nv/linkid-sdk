/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import com.lyndir.lhunath.opal.system.util.ObjectMeta;
import java.io.Serializable;
import org.apache.commons.lang.builder.*;


/**
 * <h2>{@link AttributeSDK}</h2>
 * <p/>
 * <p> <i>Nov 29, 2010</i>
 * <p/>
 * SDK Attribute.</p>
 *
 * @author wvdhaute
 */
@ObjectMeta
public class AttributeSDK<T extends Serializable> implements Serializable {

    private       String id;
    private final String name;
    private       T      value;

    public AttributeSDK(final String name) {

        this( name, null );
    }

    public AttributeSDK(final String name, final T value) {

        this( null, name, value );
    }

    public AttributeSDK(final String id, final String name, final T value) {

        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public T getValue() {

        return value;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public void setValue(final T value) {

        this.value = value;
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
        return new EqualsBuilder().append( name, rhs.name ).append( id, rhs.id ).append( value, rhs.value ).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append( name ).append( id ).append( value ).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder( this ).append( "name", name ).append( "id", id ).append( "value", value ).toString();
    }
}

