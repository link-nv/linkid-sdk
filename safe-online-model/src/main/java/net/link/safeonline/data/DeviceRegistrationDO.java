package net.link.safeonline.data;

import java.util.List;

import net.link.safeonline.entity.DeviceEntity;


public class DeviceRegistrationDO {

    private DeviceEntity      device;

    private String            friendlyName;

    private List<AttributeDO> attribute;


    public DeviceRegistrationDO(DeviceEntity device, String friendlyName, List<AttributeDO> attribute) {

        this.device = device;
        this.friendlyName = friendlyName;
        this.attribute = attribute;
    }

    public DeviceEntity getDevice() {

        return this.device;
    }

    public String getFriendlyName() {

        return this.friendlyName;
    }

    public List<AttributeDO> getAttribute() {

        return this.attribute;
    }

    public boolean isRegistrable() {

        return null != this.device.getRegistrationPath() && this.device.getRegistrationPath().length() > 0;
    }

    public boolean isUpdatable() {

        return null != this.device.getUpdatePath() && this.device.getUpdatePath().length() > 0;
    }

    public boolean isRemovable() {

        return null != this.device.getRemovalPath() && this.device.getRemovalPath().length() > 0;
    }

}
