package net.link.safeonline.data;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceRegistrationDO {

    private DeviceEntity device;

    private String       friendlyName;

    private AttributeDO  attribute;

    private boolean      disabled;

    private long         attributeIndex;


    public DeviceRegistrationDO(DeviceEntity device, String friendlyName, AttributeDO attribute, boolean disabled) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.attribute = attribute;
        this.disabled = disabled;
    }

    public DeviceRegistrationDO(DeviceEntity device, AttributeDO attribute, boolean disabled, long attributeIndex) {

        this.device = device;
        this.attribute = attribute;
        this.disabled = disabled;
        this.attributeIndex = attributeIndex;
    }

    public DeviceEntity getDevice() {

        return device;
    }

    public String getFriendlyName() {

        return friendlyName;
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
