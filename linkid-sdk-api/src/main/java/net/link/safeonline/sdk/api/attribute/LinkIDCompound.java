/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import java.io.Serializable;
import java.util.List;


/**
 * <h2>{@link LinkIDCompound}</h2>
 * <p/>
 * <p> <i>Nov 29, 2010</i>
 * <p/>
 * Compound Attribute Value class. </p>
 *
 * @author wvdhaute
 */
public class LinkIDCompound implements Serializable {

    private       String                             description;
    private final List<? extends LinkIDAttribute<?>> members;

    public LinkIDCompound(final List<? extends LinkIDAttribute<?>> members) {

        this( null, members );
    }

    public LinkIDCompound(final String description, final List<? extends LinkIDAttribute<?>> members) {

        this.description = description;
        this.members = members;
    }

    /**
     * @return list of this compound value's members
     */
    public List<? extends LinkIDAttribute<? extends Serializable>> getMembers() {

        return members;
    }

    /**
     * @param attributeName attribute name of member attribute we are fetching.
     *
     * @return specific member with specified attribute name
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> LinkIDAttribute<T> findMember(String attributeName) {

        for (LinkIDAttribute<?> member : members) {
            if (member.getName().equals( attributeName ))
                return (LinkIDAttribute<T>) member;
        }

        return null;
    }

    /**
     * @param attributeName attribute name of member attribute we are fetching.
     *
     * @return specific member with specified attribute name
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> LinkIDAttribute<T> getMember(String attributeName) {

        LinkIDAttribute<T> member = findMember( attributeName );
        if (null == member)
            throw new RuntimeException( String.format( "Unknown compound member %s", attributeName ) );

        return member;
    }

    public String getDescription() {

        if (null != description && description.length() > 0)
            return description;

        return null;
    }

    public void setDescription(final String description) {

        this.description = description;
    }
}

