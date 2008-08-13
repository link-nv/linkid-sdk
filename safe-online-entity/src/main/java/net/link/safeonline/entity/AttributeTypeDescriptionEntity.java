/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.AttributeTypeDescriptionEntity.QUERY_WHERE_ATTRIBUTE_TYPE;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "attribute_type_description")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_ATTRIBUTE_TYPE, query = "SELECT attribDesc "
        + "FROM AttributeTypeDescriptionEntity AS attribDesc " + "WHERE attribDesc.attributeType = :attributeType") })
public class AttributeTypeDescriptionEntity implements Serializable {

    public static final String         QUERY_WHERE_ATTRIBUTE_TYPE = "atd.at";

    private static final long          serialVersionUID           = 1L;

    private AttributeTypeDescriptionPK pk;

    private AttributeTypeEntity        attributeType;

    private String                     language;

    private String                     name;

    private String                     description;


    public AttributeTypeDescriptionEntity() {

        // empty
    }

    public AttributeTypeDescriptionEntity(AttributeTypeEntity attributeType, String language, String name,
            String description) {

        this.pk = new AttributeTypeDescriptionPK(attributeType.getName(), language);
        this.attributeType = attributeType;
        this.language = language;
        this.name = name;
        this.description = description;
    }


    public static final String ATTRIBUTE_TYPE_COLUMN_NAME = "attribute_type";

    public static final String LANGUAGE_COLUMN_NAME       = "language";


    @EmbeddedId
    @AttributeOverrides( {
            @AttributeOverride(name = "attributeType", column = @Column(name = ATTRIBUTE_TYPE_COLUMN_NAME)),
            @AttributeOverride(name = "language", column = @Column(name = LANGUAGE_COLUMN_NAME)) })
    public AttributeTypeDescriptionPK getPk() {

        return this.pk;
    }

    public void setPk(AttributeTypeDescriptionPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = ATTRIBUTE_TYPE_COLUMN_NAME, insertable = false, updatable = false)
    public AttributeTypeEntity getAttributeType() {

        return this.attributeType;
    }

    public void setAttributeType(AttributeTypeEntity attributeType) {

        this.attributeType = attributeType;
    }

    public String getDescription() {

        return this.description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    @Basic(optional = false)
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Column(name = LANGUAGE_COLUMN_NAME, insertable = false, updatable = false)
    public String getLanguage() {

        return this.language;
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    @Transient
    public String getAttributeTypeName() {

        return this.pk.getAttributeType();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof AttributeTypeDescriptionEntity)
            return false;
        AttributeTypeDescriptionEntity rhs = (AttributeTypeDescriptionEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).append("name", this.name).append("description",
                this.description).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_ATTRIBUTE_TYPE)
        List<AttributeTypeDescriptionEntity> listDescriptions(
                @QueryParam("attributeType") AttributeTypeEntity attributeType);
    }
}
