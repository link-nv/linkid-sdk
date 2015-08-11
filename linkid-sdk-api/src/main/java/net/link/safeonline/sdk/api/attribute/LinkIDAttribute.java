/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * <h2>{@link LinkIDAttribute}</h2>
 * <p/>
 * <p> <i>Nov 29, 2010</i>
 * <p/>
 * SDK Attribute.</p>
 *
 * @author wvdhaute
 */
public class LinkIDAttribute<T extends Serializable> implements Serializable {

    private String id;
    private String name;
    private T      value;

    public LinkIDAttribute() {

    }

    public LinkIDAttribute(final String name) {

        this( name, null );
    }

    public LinkIDAttribute(final String name, final T value) {

        this( null, name, value );
    }

    public LinkIDAttribute(final String id, final String name, final T value) {

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

    public void setName(final String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof LinkIDAttribute))
            return false;
        LinkIDAttribute rhs = (LinkIDAttribute) obj;

        return new EqualsBuilder().append( id, rhs.getId() ).append( name, rhs.getName() ).append( value, rhs.getValue() ).build();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append( id ).append( name ).append( value ).build();
    }
}

