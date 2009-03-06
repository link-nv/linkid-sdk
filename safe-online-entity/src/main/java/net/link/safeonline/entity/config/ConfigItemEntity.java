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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "config_item", uniqueConstraints = @UniqueConstraint(columnNames = { "configGroup", "name" }))
@NamedQueries( {
        @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigItemEntity c"),
        @NamedQuery(name = ConfigItemEntity.QUERY_GET_ITEMS, query = "FROM ConfigItemEntity c WHERE c.configGroup = :group"),
        @NamedQuery(name = ConfigItemEntity.QUERY_GET_ITEM, query = "FROM ConfigItemEntity c WHERE c.configGroup.name = :groupName AND c.name = :name") })
public class ConfigItemEntity implements Serializable {

    private static final long           serialVersionUID = 1L;

    public static final String          QUERY_LIST_ALL   = "cie.list";
    public static final String          QUERY_GET_ITEMS  = "cie.items";
    public static final String          QUERY_GET_ITEM   = "cie.item";

    private int                         id;

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
        values = new LinkedList<ConfigItemValueEntity>();
        valueIndex = 0;
        this.valueType = valueType;
        this.multipleChoice = multipleChoice;
        this.configGroup = configGroup;
    }

    @Id
    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "configGroup")
    public ConfigGroupEntity getConfigGroup() {

        return configGroup;
    }

    public void setConfigGroup(ConfigGroupEntity configGroup) {

        this.configGroup = configGroup;
    }

    @Column(name = "name")
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @OneToMany(mappedBy = "configItem", fetch = FetchType.EAGER)
    public List<ConfigItemValueEntity> getValues() {

        return values;
    }

    public void setValues(List<ConfigItemValueEntity> values) {

        this.values = values;
    }

    @Transient
    public String getValue() {

        if (values.isEmpty())
            return null;

        if (null == value) {
            value = values.get(valueIndex).getValue();
        }

        return value;
    }

    @Transient
    public void setValue(String value) {

        this.value = value;
    }

    public int getValueIndex() {

        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {

        this.valueIndex = valueIndex;
    }

    public String getValueType() {

        return valueType;
    }

    public void setValueType(String valueType) {

        this.valueType = valueType;
    }

    public boolean isMultipleChoice() {

        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {

        this.multipleChoice = multipleChoice;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("id", id).toString();
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
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {

        return id;
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<ConfigItemEntity> listConfigItems();

        @QueryMethod(QUERY_GET_ITEMS)
        List<ConfigItemEntity> getConfigItems(@QueryParam("group") ConfigGroupEntity group);

        @QueryMethod(QUERY_GET_ITEM)
        ConfigItemEntity getConfigItem(@QueryParam("groupName") String groupName, @QueryParam("name") String name);
    }
}
