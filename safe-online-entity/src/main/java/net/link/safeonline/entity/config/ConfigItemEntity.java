/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import static net.link.safeonline.entity.config.ConfigItemEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "config_item")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigItemEntity c") })
public class ConfigItemEntity implements Serializable {

    private static final long           serialVersionUID      = 1L;

    public static final String          QUERY_LIST_ALL        = "cie.list";

    public static final String          GROUP_COLUMN_NAME     = "configGroup";
    public static final String          ITEM_NAME_COLUMN_NAME = "name";

    private ConfigItemPK                pk;

    private String                      name;

    private List<ConfigItemValueEntity> values;

    private int                         valueIndex;

    private boolean                     multipleChoice;

    private String                      valueType;

    private ConfigGroupEntity           configGroup;

    @Transient
    String                              value;


    public ConfigItemEntity() {

        // empty
    }

    public ConfigItemEntity(String name, String valueType, boolean multipleChoice, ConfigGroupEntity configGroup) {

        this.name = name;
        this.values = new LinkedList<ConfigItemValueEntity>();
        this.valueIndex = 0;
        this.valueType = valueType;
        this.multipleChoice = multipleChoice;
        this.configGroup = configGroup;
        this.pk = new ConfigItemPK(configGroup.getName(), name);
    }

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = ConfigItemPK.PK_GROUP_NAME, column = @Column(name = GROUP_COLUMN_NAME)),
            @AttributeOverride(name = ConfigItemPK.PK_NAME, column = @Column(name = ITEM_NAME_COLUMN_NAME)) })
    public ConfigItemPK getPk() {

        return this.pk;
    }

    public void setPk(ConfigItemPK pk) {

        this.pk = pk;
    }

    @ManyToOne
    @JoinColumn(name = GROUP_COLUMN_NAME, insertable = false, updatable = false)
    public ConfigGroupEntity getConfigGroup() {

        return this.configGroup;
    }

    public void setConfigGroup(ConfigGroupEntity configGroup) {

        this.configGroup = configGroup;
    }

    @Column(name = ITEM_NAME_COLUMN_NAME, insertable = false, updatable = false)
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @OneToMany(mappedBy = "configItem", fetch = FetchType.EAGER)
    public List<ConfigItemValueEntity> getValues() {

        return this.values;
    }

    public void setValues(List<ConfigItemValueEntity> values) {

        this.values = values;
    }

    @Transient
    public String getValue() {

        if (this.values.isEmpty())
            return null;

        if (null == this.value) {
            this.value = this.values.get(this.valueIndex).getValue();
        }

        return this.value;
    }

    @Transient
    public void setValue(String value) {

        this.value = value;
    }

    public int getValueIndex() {

        return this.valueIndex;
    }

    public void setValueIndex(int valueIndex) {

        this.valueIndex = valueIndex;
    }

    public String getValueType() {

        return this.valueType;
    }

    public void setValueType(String valueType) {

        this.valueType = valueType;
    }

    public boolean isMultipleChoice() {

        return this.multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {

        this.multipleChoice = multipleChoice;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof ConfigItemEntity)
            return false;
        ConfigItemEntity rhs = (ConfigItemEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<ConfigItemEntity> listConfigItems();
    }
}
