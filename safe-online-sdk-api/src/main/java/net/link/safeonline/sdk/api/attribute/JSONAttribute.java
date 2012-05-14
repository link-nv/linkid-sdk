package net.link.safeonline.sdk.api.attribute;

import java.util.Date;
import java.util.List;


/**
 * TODO description
 * <p/>
 * Date: 14/05/12
 * Time: 10:44
 *
 * @author: sgdesmet
 */
public class JSONAttribute {

    private       String id;
    private       String name;
    private       DataType type;
    private       String value;
    private       List<JSONAttribute> members;

    public String getId() {

        return id;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public DataType getType() {

        return type;
    }

    public void setType(final DataType type) {

        this.type = type;
    }

    public String getValue() {

        return value;
    }

    public void setValue(final String value) {

        this.value = value;
    }

    public List<JSONAttribute> getMembers() {

        return members;
    }

    public void setMembers(final List<JSONAttribute> members) {

        this.members = members;
    }
}
