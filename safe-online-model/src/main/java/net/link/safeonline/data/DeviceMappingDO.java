package net.link.safeonline.data;

import java.util.List;

import net.link.safeonline.entity.DeviceMappingEntity;


public class DeviceMappingDO {

    private DeviceMappingEntity deviceMapping;

    private String              friendlyName;

    private List<AttributeDO>   attribute;


    public DeviceMappingDO(DeviceMappingEntity deviceMapping, String friendlyName, List<AttributeDO> attribute) {

        this.deviceMapping = deviceMapping;
        this.friendlyName = friendlyName;
        this.attribute = attribute;
    }

    public DeviceMappingEntity getDeviceMapping() {

        return this.deviceMapping;
    }

    public void setDeviceMapping(DeviceMappingEntity deviceMapping) {

        this.deviceMapping = deviceMapping;
    }

    public String getFriendlyName() {

        return this.friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }

    public List<AttributeDO> getAttribute() {

        return this.attribute;
    }

    public void setAttribute(List<AttributeDO> attribute) {

        this.attribute = attribute;
    }

    public boolean isRegistrable() {

        return null != this.deviceMapping.getDevice().getRegistrationPath();
    }

    public boolean isUpdatable() {

        return null != this.deviceMapping.getDevice().getUpdatePath();
    }

    public boolean isRemovable() {

        return null != this.deviceMapping.getDevice().getRemovalPath();
    }

}
