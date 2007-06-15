package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "application_identity_attribute")
public class ApplicationIdentityAttributeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private ApplicationIdentityAttributePK pk;

	private boolean required;

	private boolean dataMining;

	private ApplicationIdentityEntity applicationIdentity;

	private AttributeTypeEntity attributeType;

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String ATTRIBUTE_TYPE_NAME = "attributeType";

	public static final String IDENTITY_VERSION_NAME = "identityVersion";

	public ApplicationIdentityAttributeEntity() {
		// empty
	}

	public ApplicationIdentityAttributeEntity(
			ApplicationIdentityEntity applicationIdentity,
			AttributeTypeEntity attributeType, boolean required,
			boolean dataMining) {
		String applicationName = applicationIdentity.getApplication().getName();
		long identityVersion = applicationIdentity.getIdentityVersion();
		String attributeTypeName = attributeType.getName();
		this.pk = new ApplicationIdentityAttributePK(applicationName,
				identityVersion, attributeTypeName);
		this.attributeType = attributeType;
		this.required = required;
		this.dataMining = dataMining;
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = APPLICATION_COLUMN_NAME)),
			@AttributeOverride(name = "identityVersion", column = @Column(name = IDENTITY_VERSION_NAME)),
			@AttributeOverride(name = "attributeTypeName", column = @Column(name = ATTRIBUTE_TYPE_NAME)) })
	public ApplicationIdentityAttributePK getPk() {
		return this.pk;
	}

	public void setPk(ApplicationIdentityAttributePK pk) {
		this.pk = pk;
	}

	public boolean isRequired() {
		return this.required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@ManyToOne(optional = false)
	@JoinColumns( {
			@JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false, referencedColumnName = ApplicationIdentityEntity.APPLICATION_COLUMN_NAME),
			@JoinColumn(name = IDENTITY_VERSION_NAME, insertable = false, updatable = false, referencedColumnName = ApplicationIdentityEntity.IDENTITY_VERSION_COLUMN_NAME) })
	public ApplicationIdentityEntity getApplicationIdentity() {
		return this.applicationIdentity;
	}

	public void setApplicationIdentity(
			ApplicationIdentityEntity applicationIdentity) {
		this.applicationIdentity = applicationIdentity;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = ATTRIBUTE_TYPE_NAME, insertable = false, updatable = false)
	public AttributeTypeEntity getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(AttributeTypeEntity attributeType) {
		this.attributeType = attributeType;
	}

	@Transient
	public String getApplicationName() {
		return this.pk.getApplication();
	}

	@Transient
	public long getIdentityVersion() {
		return this.pk.getIdentityVersion();
	}

	@Transient
	public String getAttributeTypeName() {
		return this.pk.getAttributeTypeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof ApplicationIdentityAttributeEntity) {
			return false;
		}
		ApplicationIdentityAttributeEntity rhs = (ApplicationIdentityAttributeEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	public boolean isDataMining() {
		return dataMining;
	}

	public void setDataMining(boolean dataMining) {
		this.dataMining = dataMining;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pk", this.pk).append("required", this.required)
				.toString();
	}
}