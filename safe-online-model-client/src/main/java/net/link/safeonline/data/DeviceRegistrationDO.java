package net.link.safeonline.data;

import java.io.Serializable;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceRegistrationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private DeviceEntity      device;

    private String            friendlyName;

    private String            id;

    private AttributeDO       attribute;

    private boolean           disabled;

    private long              attributeIndex;


    public DeviceRegistrationDO(DeviceEntity device, String friendlyName, String id, AttributeDO attribute, boolean disabled) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.id = id;
        this.attribute = attribute;
        this.disabled = disabled;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getId() {

        return id;
    }

    public AttributeDO getAttribute() {

        return attribute;
    }

    public boolean isDisabled() {

        return disabled;
    }

    public void setDisabled(boolean disabled) {

        this.disabled = disabled;
    }

    public long getAttributeIndex() {

        return attributeIndex;
    }

}
