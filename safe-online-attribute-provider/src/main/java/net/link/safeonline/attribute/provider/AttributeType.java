package net.link.safeonline.attribute.provider;

import com.lyndir.lhunath.opal.system.util.MetaObject;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class AttributeType extends MetaObject implements Serializable {

    private final String   name;
    private final DataType type;
    private final String   providerJndi;
    private final boolean  userVisible;
    private final boolean  userEditable;
    private final boolean  userRemovable;
    private final boolean  multivalued;
    private final boolean  mappable;

    // only sensible for compound members
    private final boolean             required;
    private final List<AttributeType> members;

    public AttributeType(final String name) {

        this( name, null, null, false, false, false, false, false, false );
    }

    public AttributeType(final String name, final DataType dataType) {

        this( name, dataType, null, false, false, false, false, false, false );
    }

    public AttributeType(final String name, final DataType dataType, boolean multivalued) {

        this( name, dataType, null, false, false, false, multivalued, false, false );
    }

    public AttributeType(String name, DataType type, String providerJndi, boolean userVisible, boolean userEditable, boolean multivalued,
                         boolean mappable, boolean required) {

        this( name, type, providerJndi, userVisible, userEditable, false, multivalued, mappable, required );
    }

    public AttributeType(String name, DataType type, String providerJndi, boolean userVisible, boolean userEditable, boolean userRemovable,
                         boolean multivalued, boolean mappable, boolean required) {

        this.name = name;
        this.type = type;
        this.providerJndi = providerJndi;
        this.userVisible = userVisible;
        this.userEditable = userEditable;
        this.userRemovable = userRemovable;
        this.multivalued = multivalued;
        this.mappable = mappable;
        this.required = required;
        members = new LinkedList<AttributeType>();
    }

    public String getName() {

        return name;
    }

    public DataType getType() {

        return type;
    }

    public String getProviderJndi() {

        return providerJndi;
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

    public boolean isCompound() {

        return type == DataType.COMPOUNDED;
    }

    public boolean isMappable() {

        return mappable;
    }

    public boolean isRequired() {

        return required;
    }

    public List<AttributeType> getMembers() {

        return members;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!(obj instanceof AttributeType))
            return false;
        AttributeType rhs = (AttributeType) obj;

        return new EqualsBuilder().append( name, rhs.name )
                                  .append( type, rhs.type )
                                  .append( providerJndi, rhs.providerJndi )
                                  .append( userVisible, rhs.userVisible )
                                  .append( userEditable, rhs.userEditable )
                                  .append( userRemovable, rhs.userRemovable )
                                  .append( multivalued, rhs.multivalued )
                                  .append( mappable, rhs.mappable )
                                  .append( required, rhs.required )
                                  .append( members, rhs.members )
                                  .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append( name ).append( type ).toHashCode();
    }
}
