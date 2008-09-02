/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.AttributeCacheEntity.ATTRIBUTE_TYPE_PARAM;
import static net.link.safeonline.entity.AttributeCacheEntity.QUERY_ALL;
import static net.link.safeonline.entity.AttributeCacheEntity.QUERY_DELETE_WHERE_OLDER;
import static net.link.safeonline.entity.AttributeCacheEntity.QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE;
import static net.link.safeonline.entity.AttributeCacheEntity.SUBJECT_PARAM;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
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
 * Attribute Cache JPA Entity. Representing cached attributes values for remote attributes. The TTL is configured in the
 * {@link AttributeTypeEntity}.
 * 
 * Sits as many-to-many between {@link AttributeTypeEntity} and {@link SubjectEntity}. Multi-valued attributes are
 * implemented via {@link #getAttributeIndex()}.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "attribute_cache")
@NamedQueries( {
        @NamedQuery(name = QUERY_ALL, query = "SELECT a FROM AttributeCacheEntity AS a"),
        @NamedQuery(name = QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE, query = "SELECT a "
                + "FROM AttributeCacheEntity AS a " + "WHERE a.subject = :" + SUBJECT_PARAM
                + " AND a.attributeType = :" + ATTRIBUTE_TYPE_PARAM + " ORDER BY a.attributeIndex"),
        @NamedQuery(name = QUERY_DELETE_WHERE_OLDER, query = "DELETE FROM AttributeCacheEntity AS a "
                + "WHERE a.entryDate < :ageLimit") })
public class AttributeCacheEntity implements Serializable {

    private static final long   serialVersionUID                       = 1L;

    public static final String  SUBJECT_PARAM                          = "subject";

    public static final String  ATTRIBUTE_TYPE_PARAM                   = "attributeType";

    public static final String  QUERY_ALL                              = "attr.cache.all";

    public static final String  QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE = "attr.cache.subject.at";

    public static final String  QUERY_DELETE_WHERE_OLDER               = "attr.cache.delete.old";

    public static final String  ATTRIBUTE_INDEX_COLUMN_NAME            = "attribute_index";

    public static final String  ATTRIBUTE_TYPE_COLUMN_NAME             = "attribute_type";

    public static final String  SUBJECT_COLUMN_NAME                    = "subject";

    private AttributePK         pk;

    private AttributeTypeEntity attributeType;

    private SubjectEntity       subject;

    private Date                entryDate;

    private long                attributeIndex;

    private String              stringValue;

    private Boolean             booleanValue;

    private Integer             integerValue;

    private Double              doubleValue;

    private Date                dateValue;


    public AttributeCacheEntity() {

        // empty
    }

    public AttributeCacheEntity(AttributeTypeEntity attributeType, SubjectEntity subject, long attributeIdx) {

        this.attributeType = attributeType;
        this.subject = subject;
        this.attributeIndex = attributeIdx;
        this.pk = new AttributePK(attributeType, subject, attributeIdx);
        this.entryDate = new Date(System.currentTimeMillis());
    }

    @EmbeddedId
    @AttributeOverrides( {
            @AttributeOverride(name = "attributeType", column = @Column(name = ATTRIBUTE_TYPE_COLUMN_NAME)),
            @AttributeOverride(name = "subject", column = @Column(name = SUBJECT_COLUMN_NAME)),
            @AttributeOverride(name = "attributeIndex", column = @Column(name = ATTRIBUTE_INDEX_COLUMN_NAME)) })
    public AttributePK getPk() {

        return this.pk;
    }

    public void setPk(AttributePK pk) {

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

    @ManyToOne(optional = false)
    @JoinColumn(name = SUBJECT_COLUMN_NAME, insertable = false, updatable = false)
    public SubjectEntity getSubject() {

        return this.subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }

    public Date getEntryDate() {

        return this.entryDate;
    }

    public void setEntryDate(Date entryDate) {

        this.entryDate = entryDate;
    }

    /**
     * The attribute index is used for implementing the multi-valued attributes. For single-value attributes that
     * attribute index is zero.
     * 
     */
    @Column(name = ATTRIBUTE_INDEX_COLUMN_NAME, insertable = false, updatable = false)
    public long getAttributeIndex() {

        return this.attributeIndex;
    }

    public void setAttributeIndex(long attributeIndex) {

        this.attributeIndex = attributeIndex;
    }

    public String getStringValue() {

        return this.stringValue;
    }

    public void setStringValue(String stringValue) {

        this.stringValue = stringValue;
    }

    public Boolean getBooleanValue() {

        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {

        this.booleanValue = booleanValue;
    }

    @Temporal(TemporalType.DATE)
    public Date getDateValue() {

        return this.dateValue;
    }

    public void setDateValue(Date dateValue) {

        this.dateValue = dateValue;
    }

    public Double getDoubleValue() {

        return this.doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {

        this.doubleValue = doubleValue;
    }

    public Integer getIntegerValue() {

        return this.integerValue;
    }

    public void setIntegerValue(Integer integerValue) {

        this.integerValue = integerValue;
    }


    private transient List<AttributeCacheEntity> members;


    /**
     * We don't manage the member attributes of a compounded attribute directly via the database because the
     * relationship is to complex to express. This field is filled in by the DAO layer upon request.
     * 
     */
    @Transient
    public List<AttributeCacheEntity> getMembers() {

        if (null == this.members) {
            this.members = new LinkedList<AttributeCacheEntity>();
        }
        return this.members;
    }

    public void setMembers(List<AttributeCacheEntity> members) {

        this.members = members;
    }

    /**
     * Generic data mapping can be done via {@link #getValue()} and {@link #setValue(Object)}.
     * 
     */
    @Transient
    public Object getValue() {

        DatatypeType datatype = this.attributeType.getType();
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
            default:
                throw new EJBException("datatype not supported: " + datatype);
        }
    }

    @Transient
    public void setValue(Object value) {

        DatatypeType datatype = this.attributeType.getType();
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
            default:
                throw new EJBException("datatype not supported: " + datatype);
        }
    }

    @Transient
    public boolean isEmpty() {

        DatatypeType datatype = this.attributeType.getType();
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

        this.booleanValue = null;
        this.dateValue = null;
        this.doubleValue = null;
        this.integerValue = null;
        this.stringValue = null;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof AttributeCacheEntity)
            return false;
        AttributeCacheEntity rhs = (AttributeCacheEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).append("entry date", this.entryDate).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_SUBJECT_AND_ATTRIBUTE_TYPE)
        List<AttributeCacheEntity> listAttributes(@QueryParam(SUBJECT_PARAM) SubjectEntity subject,
                @QueryParam(ATTRIBUTE_TYPE_PARAM) AttributeTypeEntity attributeType);

        @UpdateMethod(QUERY_DELETE_WHERE_OLDER)
        void deleteWhereOlder(@QueryParam("ageLimit") Date ageLimit);

        @QueryMethod(QUERY_ALL)
        List<AttributeCacheEntity> listAttributes();
    }
}
