/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.attribute;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDAttributeType implements Serializable {

    private String                    name;
    private LinkIDDataType            type;
    private String                    providerJndi;            // backwards compatibility, ios clients crashes on it...
    private boolean                   userVisible;
    private boolean                   userEditable;
    private boolean                   userRemovable;
    private boolean                   multivalued;
    private boolean                   required;
    //
    // only sensible for compounds
    private boolean                   compoundMember;
    private List<LinkIDAttributeType> members;

    public LinkIDAttributeType() {

        this.members = new LinkedList<>();
    }

    public LinkIDAttributeType(final String name) {

        this( name, null, false, false, false, false, false, false );
    }

    public LinkIDAttributeType(String name, LinkIDDataType type, boolean userVisible, boolean userEditable, boolean multivalued, boolean required) {

        this( name, type, userVisible, userEditable, false, multivalued, false, required );
    }

    public LinkIDAttributeType(String name, LinkIDDataType type, boolean userVisible, boolean userEditable, boolean userRemovable, boolean multivalued,
                               boolean compoundMember, boolean required) {

        this.name = name;
        this.type = type;
        this.providerJndi = "";
        this.userVisible = userVisible;
        this.userEditable = userEditable;
        this.userRemovable = userRemovable;
        this.multivalued = multivalued;
        this.compoundMember = compoundMember;
        this.required = required;
        this.members = new LinkedList<>();
    }

    public String getName() {

        return name;
    }

    public LinkIDDataType getType() {

        return type;
    }

    public boolean isUserVisible() {

        return userVisible;
    }

    public boolean isUserEditable() {

        return userEditable;
    }

    public boolean isUserRemovable() {

        return userRemovable;
    }

    public boolean isMultivalued() {

        return multivalued;
    }

    @Deprecated
    public boolean isCompound() {

        return type == LinkIDDataType.COMPOUNDED;
    }

    public boolean isCompoundMember() {

        return compoundMember;
    }

    public boolean isRequired() {

        return required;
    }

    public List<LinkIDAttributeType> getMembers() {

        return members;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public void setType(final LinkIDDataType type) {

        this.type = type;
    }

    public void setUserVisible(final boolean userVisible) {

        this.userVisible = userVisible;
    }

    public void setUserEditable(final boolean userEditable) {

        this.userEditable = userEditable;
    }

    public void setUserRemovable(final boolean userRemovable) {

        this.userRemovable = userRemovable;
    }

    public void setMultivalued(final boolean multivalued) {

        this.multivalued = multivalued;
    }

    public void setCompoundMember(final boolean compoundMember) {

        this.compoundMember = compoundMember;
    }

    public void setRequired(final boolean required) {

        this.required = required;
    }

    public void setMembers(final List<LinkIDAttributeType> members) {

        this.members = members;
    }

    public String getProviderJndi() {

        return providerJndi;
    }

    public void setProviderJndi(final String providerJndi) {

        this.providerJndi = providerJndi;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof LinkIDAttributeType))
            return false;
        LinkIDAttributeType rhs = (LinkIDAttributeType) obj;

        return name.equals( rhs.getName() );
    }

    @Override
    public int hashCode() {

        return name.hashCode();
    }

    @Override
    public String toString() {

        return String.format( "%s, type=%s", name, type );
    }
}
