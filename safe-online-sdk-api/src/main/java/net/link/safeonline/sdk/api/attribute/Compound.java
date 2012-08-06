/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.api.attribute;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import com.lyndir.lhunath.opal.system.util.MetaObject;
import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link Compound}</h2>
 * <p/>
 * <p> <i>Nov 29, 2010</i>
 * <p/>
 * Compound Attribute Value class. </p>
 *
 * @author wvdhaute
 */
public class Compound extends MetaObject implements Serializable {

    private       String                          description;
    private final List<? extends AttributeSDK<?>> members;

    public Compound(final List<? extends AttributeSDK<?>> members) {

        this( null, members );
    }

    public Compound(@Nullable final String description, final List<? extends AttributeSDK<?>> members) {

        this.description = description;
        this.members = members;
    }

    /**
     * @return list of this compound value's members
     */
    public List<? extends AttributeSDK<? extends Serializable>> getMembers() {

        return members;
    }

    /**
     * @param attributeName attribute name of member attribute we are fetching.
     *
     * @return specific member with specified attribute name
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Serializable> AttributeSDK<T> findMember(String attributeName) {

        for (AttributeSDK<?> member : members) {
            if (member.getName().equals( attributeName ))
                return (AttributeSDK<T>) member;
        }

        return null;
    }

    /**
     * @param attributeName attribute name of member attribute we are fetching.
     *
     * @return specific member with specified attribute name
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> AttributeSDK<T> getMember(String attributeName) {

        AttributeSDK<T> member = findMember( attributeName );
        if (null == member)
            throw new InternalInconsistencyException( String.format( "Unknown compound member %s", attributeName ) );

        return member;
    }

    @Nullable
    public String getDescription() {

        if (null != description && !description.isEmpty())
            return description;

        return null;
    }

    public void setDescription(final String description) {

        this.description = description;
    }
}

