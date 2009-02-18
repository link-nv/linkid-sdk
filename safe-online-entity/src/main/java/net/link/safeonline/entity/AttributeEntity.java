/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.AttributeEntity.ATTRIBUTE_TYPE_PARAM;
import static net.link.safeonline.entity.AttributeEntity.DELETE_WHERE_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeEntity.DELETE_WHERE_SUBJECT;
import static net.link.safeonline.entity.AttributeEntity.MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_PREFIX_AND_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeEntity.QUERY_WHERE_SUBJECT_AND_VISIBLE;
import static net.link.safeonline.entity.AttributeEntity.SUBJECT_PARAM;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Attribute JPA Entity. Sits as many-to-many between {@link AttributeTypeEntity} and {@link SubjectEntity}. Multi-valued attributes are
 * implemented via {@link #getAttributeIndex()}.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "attribute")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT attribute FROM AttributeEntity AS attribute "
                + "WHERE attribute.subject = :" + SUBJECT_PARAM + " ORDER BY attribute.attributeType, attribute.attributeIndex"),
        @NamedQuery(name = QUERY_WHERE_SUBJECT_AND_VISIBLE, query = "SELECT attribute FROM AttributeEntity AS attribute "
                + "WHERE attribute.subject = :" + SUBJECT_PARAM + " AND attribute.attributeType.userVisible = TRUE "
                + "ORDER BY attribute.attributeType, attribute.attributeIndex"),
        @NamedQuery(name = QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE, query = "SELECT attribute FROM AttributeEntity AS attribute "
                + "WHERE attribute.subject = :" + SUBJECT_PARAM + " AND attribute.attributeType = :" + ATTRIBUTE_TYPE_PARAM
                + " ORDER BY attribute.attributeIndex"),
        @NamedQuery(name = QUERY_WHERE_PREFIX_AND_ATTRIBUTE_TYPE, query = "SELECT attribute FROM AttributeEntity AS attribute "
                + "WHERE SUBSTRING(attribute.stringValue,1,LENGTH(:prefix)) = :prefix" + " AND attribute.attributeType = :"
                + ATTRIBUTE_TYPE_PARAM + " ORDER BY attribute.stringValue"),
        @NamedQuery(name = MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE, query = "SELECT MAX(attribute.attributeIndex) FROM AttributeEntity AS attribute "
                + "WHERE attribute.subject = :" + SUBJECT_PARAM + " AND attribute.attributeType = :" + ATTRIBUTE_TYPE_PARAM),
        @NamedQuery(name = DELETE_WHERE_SUBJECT, query = "DELETE FROM AttributeEntity AS attribute " + "WHERE attribute.subject = :"
                + SUBJECT_PARAM),
        @NamedQuery(name = DELETE_WHERE_ATTRIBUTE_TYPE, query = "DELETE FROM AttributeEntity AS attribute "
                + "WHERE attribute.attributeType = :" + ATTRIBUTE_TYPE_PARAM) })
public class AttributeEntity implements Serializable {

    private static final long   serialVersionUID                        = 1L;

    public static final String  QUERY_WHERE_SUBJECT                     = "attr.subject";

    public static final String  QUERY_WHERE_SUBJECT_AND_VISIBLE         = "attr.subject.visi";

    public static final String  QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE  = "attr.subject.at";

    public static final String  QUERY_WHERE_PREFIX_AND_ATTRIBUTE_TYPE   = "attr.pre.at";

    public static final String  MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE = "max.id.subject.at";

    public static final String  DELETE_WHERE_SUBJECT                    = "attr.del.sub";

    public static final String  DELETE_WHERE_ATTRIBUTE_TYPE             = "attr.del.at";

    public static final String  SUBJECT_PARAM                           = "subject";

    public static final String  ATTRIBUTE_TYPE_PARAM                    = "attributeType";

    private AttributePK         pk;

    private AttributeTypeEntity attributeType;

    private SubjectEntity       subject;

    private long                attributeIndex;

    private String              stringValue;

    private Boolean             booleanValue;

    private Integer             integerValue;

    private Double              doubleValue;

    private Date                dateValue;


    public AttributeEntity() {

        // empty
    }

    public AttributeEntity(AttributeTypeEntity attributeType, SubjectEntity subject, String stringValue) {

        this.stringValue = stringValue;
        this.attributeType = attributeType;
        this.subject = subject;
        pk = new AttributePK(attributeType.getName(), subject.getUserId());
    }

    public AttributeEntity(AttributeTypeEntity attributeType, SubjectEntity subject, long attributeIdx) {

        this.attributeType = attributeType;
        this.subject = subject;
        attributeIndex = attributeIdx;
        pk = new AttributePK(attributeType, subject, attributeIdx);
    }


    public static final String ATTRIBUTE_INDEX_COLUMN_NAME = "attribute_index";


    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "attributeType", column = @Column(name = ATTRIBUTE_TYPE_COLUMN_NAME)),
            @AttributeOverride(name = "subject", column = @Column(name = SUBJECT_COLUMN_NAME)),
            @AttributeOverride(name = "attributeIndex", column = @Column(name = ATTRIBUTE_INDEX_COLUMN_NAME)) })
    public AttributePK getPk() {

        return pk;
    }

    public void setPk(AttributePK pk) {

        this.pk = pk;
    }


    public static final String ATTRIBUTE_TYPE_COLUMN_NAME = "attribute_type";


    @ManyToOne(optional = false)
    @JoinColumn(name = ATTRIBUTE_TYPE_COLUMN_NAME, insertable = false, updatable = false)
    public AttributeTypeEntity getAttributeType() {

        return attributeType;
    }

    public void setAttributeType(AttributeTypeEntity attributeType) {

        this.attributeType = attributeType;
    }


    public static final String SUBJECT_COLUMN_NAME = "subject";


    @ManyToOne(optional = false)
    @JoinColumn(name = SUBJECT_COLUMN_NAME, insertable = false, updatable = false)
    public SubjectEntity getSubject() {

        return subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    /**
     * The attribute index is used for implementing the multi-valued attributes. For single-value attributes that attribute index is zero.
     * 
     */
    @Column(name = ATTRIBUTE_INDEX_COLUMN_NAME, insertable = false, updatable = false)
    public long getAttributeIndex() {

        return attributeIndex;
    }

    public void setAttributeIndex(long attributeIndex) {

        this.attributeIndex = attributeIndex;
    }

    public String getStringValue() {

        return stringValue;
    }

    public void setStringValue(String stringValue) {

        this.stringValue = stringValue;
    }

    public Boolean getBooleanValue() {

        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {

        this.booleanValue = booleanValue;
    }

    @Temporal(TemporalType.DATE)
    public Date getDateValue() {

        return dateValue;
    }

    public void setDateValue(Date dateValue) {

        this.dateValue = dateValue;
    }

    public Double getDoubleValue() {

        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {

        this.doubleValue = doubleValue;
    }

    public Integer getIntegerValue() {

        return integerValue;
    }

    public void setIntegerValue(Integer integerValue) {

        this.integerValue = integerValue;
    }

    /**
     * Generic data mapping can be done via {@link #getValue()} and {@link #setValue(Object)}.
     * 
     */
    @Transient
    public Object getValue() {

        DatatypeType datatype = attributeType.getType();
        switch (datatype) {
            case STRING:
                return getStringValue();
            case BOOLEAN:
                return getBooleanValue();
            case INTEGER:
                return getIntegerValue();
            case DOUBLE:
                return getDoubleValue();
            case DATE:
                return getDateValue();
            case COMPOUNDED:
                return getStringValue();
            default:
                throw new EJBException("datatype not supported: " + datatype);
        }
    }

    @Transient
    public void setValue(Object value) {

        DatatypeType datatype = attributeType.getType();
        switch (datatype) {
            case STRING:
                setStringValue((String) value);
            break;
            case BOOLEAN:
                setBooleanValue((Boolean) value);
            break;
            case INTEGER:
                setIntegerValue((Integer) value);
            break;
            case DOUBLE:
                setDoubleValue((Double) value);
            break;
            case DATE:
                setDateValue((Date) value);
            break;
            case COMPOUNDED:
                setStringValue((String) value);
            break;
            default:
                throw new EJBException("datatype not supported: " + datatype);
        }
    }

    @Transient
    public boolean isEmpty() {

        DatatypeType datatype = attributeType.getType();
        switch (datatype) {
            case STRING:
                String value = getStringValue();
                if (null == value)
                    return true;
                return value.length() == 0;
            case BOOLEAN:
                return null == getBooleanValue();
            case INTEGER:
                return null == getIntegerValue();
            case DOUBLE:
                return null == getDoubleValue();
            case DATE:
                return null == getDateValue();
            default:
                throw new EJBException("datatype not supported: " + datatype);
        }
    }

    @Transient
    public void clearValues() {

        booleanValue = null;
        dateValue = null;
        doubleValue = null;
        integerValue = null;
        stringValue = null;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof AttributeEntity)
            return false;
        AttributeEntity rhs = (AttributeEntity) obj;
        return new EqualsBuilder().append(pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", pk).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_SUBJECT)
        List<AttributeEntity> listAttributes(@QueryParam(SUBJECT_PARAM) SubjectEntity subject);

        @QueryMethod(QUERY_WHERE_SUBJECT_AND_VISIBLE)
        List<AttributeEntity> listVisibleAttributes(@QueryParam(SUBJECT_PARAM) SubjectEntity subject);

        @QueryMethod(QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE)
        List<AttributeEntity> listAttributes(@QueryParam(SUBJECT_PARAM) SubjectEntity subject,
                                             @QueryParam(ATTRIBUTE_TYPE_PARAM) AttributeTypeEntity attributeType);

        @QueryMethod(QUERY_WHERE_PREFIX_AND_ATTRIBUTE_TYPE)
        List<AttributeEntity> listAttributes(@QueryParam("prefix") String prefix,
                                             @QueryParam(ATTRIBUTE_TYPE_PARAM) AttributeTypeEntity attributeType);

        @QueryMethod(MAX_ID_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE)
        List<Long> listMaxIdWhereSubjectAndAttributeType(@QueryParam(SUBJECT_PARAM) SubjectEntity subject,
                                                         @QueryParam(ATTRIBUTE_TYPE_PARAM) AttributeTypeEntity attributeType);

        @UpdateMethod(DELETE_WHERE_SUBJECT)
        void deleteAttributes(@QueryParam(SUBJECT_PARAM) SubjectEntity subject);

        @UpdateMethod(DELETE_WHERE_ATTRIBUTE_TYPE)
        int deleteAttributes(@QueryParam(ATTRIBUTE_TYPE_PARAM) AttributeTypeEntity attributeType);
    }
}
