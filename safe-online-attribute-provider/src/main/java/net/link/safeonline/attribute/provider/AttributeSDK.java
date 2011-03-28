/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import com.lyndir.lhunath.lib.system.util.ObjectMeta;
import com.lyndir.lhunath.lib.system.util.ObjectUtils;
import java.io.Serializable;


/**
 * <h2>{@link AttributeSDK}</h2>
 *
 * <p> <i>Nov 29, 2010</i>
 *
 * SDK Attribute.</p>
 *
 * @author wvdhaute
 */
@ObjectMeta
public class AttributeSDK<T extends Serializable> implements Serializable {

    private final String id;
    private final String name;
    private T value;

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

    public void setValue(final T value) {

        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {

        return ObjectUtils.equals( this, obj );
    }

    @Override
    public int hashCode() {

        return ObjectUtils.hashCode( this );
    }

    @Override
    public String toString() {

        return ObjectUtils.toString( this );
    }
}

