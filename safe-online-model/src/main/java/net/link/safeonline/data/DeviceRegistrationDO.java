package net.link.safeonline.data;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceRegistrationDO {

    private DeviceEntity device;

    private String       friendlyName;

    private AttributeDO  attribute;

    private boolean      disabled;


    public DeviceRegistrationDO(DeviceEntity device, String friendlyName, AttributeDO attribute, boolean disabled) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.attribute = attribute;
        this.disabled = disabled;
    }

    public DeviceEntity getDevice() {

        return this.device;
    }

    public String getFriendlyName() {

        return this.friendlyName;
    }

    public AttributeDO getAttribute() {

        return this.attribute;
    }

    public boolean isDisabled() {

        return this.disabled;
    }

}
