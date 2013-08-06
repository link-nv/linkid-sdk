package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.api.attribute.AttributeType;
import net.link.safeonline.sdk.api.attribute.DataType;


@SuppressWarnings("UnusedDeclaration")
public class LinkIDAttribute implements Serializable {

    private AttributeType attributeType;

    // identity info
    private String  friendlyName;
    private String  groupName;
    private boolean anonymous;
    private boolean optional;
    private boolean confirmationNeeded;
    private boolean confirmed;

    // value info
    private String                id;
    private Object                value;
    private List<LinkIDAttribute> members;

    // state info
    private boolean removed;

    public LinkIDAttribute() {

        this.members = new LinkedList<LinkIDAttribute>();
    }

    public LinkIDAttribute(final String id, final AttributeType attributeType, final String friendlyName, final String groupName, final boolean anonymous,
                           final boolean optional, final boolean confirmationNeeded, final boolean confirmed, final Object value,
                           final List<LinkIDAttribute> members) {

        this.id = id;
        this.attributeType = attributeType;
        this.friendlyName = friendlyName;
        this.groupName = groupName;
        this.anonymous = anonymous;
        this.optional = optional;
        this.confirmationNeeded = confirmationNeeded;
        this.confirmed = confirmed;
        this.value = value;
        this.members = members;
        this.removed = false;
    }

    // helper methods

    public LinkIDAttribute createTemplate() {

        LinkIDAttribute templateAttribute = new LinkIDAttribute( null, attributeType, friendlyName, groupName, anonymous, optional, confirmationNeeded,
                confirmed, null, null );
        if (templateAttribute.getAttributeType().isCompound()) {
            List<LinkIDAttribute> templateMembers = new LinkedList<LinkIDAttribute>();
            for (LinkIDAttribute member : members) {
                templateMembers.add( member.createTemplate() );
            }
            templateAttribute.setMembers( templateMembers );
        }

        return templateAttribute;
    }

    public boolean isEmpty() {

        boolean empty = null == value;
        if (!empty || attributeType.getType() == DataType.COMPOUNDED) {

            switch (attributeType.getType()) {

                case STRING:
                    //noinspection ConstantConditions
                    empty = 0 == ((String) value).length();
                    break;
                case COMPOUNDED:
                    empty = false;
                    for (LinkIDAttribute member : members) {
                        if (member.isEmpty() && !member.isOptional()) {
                            empty = true;
                            break;
                        }
                    }
                    break;
                case BOOLEAN:
                case INTEGER:
                case DOUBLE:
                case DATE:
            }
        }
        return empty;
    }

    // getters/setters

    public boolean isConfirmed() {

        return confirmed;
    }

    public void setConfirmed(final boolean confirmed) {

        this.confirmed = confirmed;
    }

    public Object getValue() {

        return value;
    }

    public void setValue(final Object value) {

        this.value = value;
    }

    public List<LinkIDAttribute> getMembers() {

        return members;
    }

    public void setMembers(final List<LinkIDAttribute> members) {

        this.members = members;
    }

    public AttributeType getAttributeType() {

        return attributeType;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getGroupName() {

        return groupName;
    }

    public boolean isAnonymous() {

        return anonymous;
    }

    public boolean isOptional() {

        if (!optional)
            return optional;

        // optional -> check if it is a member, if so check member requiredness
        if (attributeType.isCompoundMember()) {

            return !attributeType.isRequired();
        } else {

            return optional;
        }
    }

    public boolean isConfirmationNeeded() {

        return confirmationNeeded;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public String getId() {

        return id;
    }

    public boolean isRemoved() {

        return removed;
    }

    public void setRemoved(final boolean removed) {

        this.removed = removed;
    }

    public void setAttributeType(final AttributeType attributeType) {

        this.attributeType = attributeType;
    }

    public void setFriendlyName(final String friendlyName) {

        this.friendlyName = friendlyName;
    }

    public void setGroupName(final String groupName) {

        this.groupName = groupName;
    }

    public void setAnonymous(final boolean anonymous) {

        this.anonymous = anonymous;
    }

    public void setOptional(final boolean optional) {

        this.optional = optional;
    }

    public void setConfirmationNeeded(final boolean confirmationNeeded) {

        this.confirmationNeeded = confirmationNeeded;
    }
}
