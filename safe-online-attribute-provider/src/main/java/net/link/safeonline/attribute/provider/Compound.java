/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider;

import java.io.Serializable;
import java.util.List;


/**
 * <h2>{@link Compound}</h2>
 *
 * <p> <i>Nov 29, 2010</i>
 *
 * Compound Attribute Value class. </p>
 *
 * @author wvdhaute
 */
public class Compound implements Serializable {

    private final List<? extends AttributeAbstract<?>> members;

    public Compound(List<? extends AttributeAbstract<?>> members) {

        this.members = members;
    }

    /**
     * @return list of this compound value's members
     */
    public List<? extends AttributeAbstract<?>> getMembers() {
        return members;
    }

    /**
     * @param attributeName attribute name of member attribute we are fetching.
     *
     * @return specific member with specified attribute name
     */
    public <T extends Serializable> AttributeAbstract<T> findMember(String attributeName) {

        for (AttributeAbstract<?> member : members) {
            if (member.getAttributeName().equals( attributeName ))
                return (AttributeAbstract<T>) member;
        }

        return null;
    }
}

