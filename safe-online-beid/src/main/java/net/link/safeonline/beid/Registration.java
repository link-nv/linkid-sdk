package net.link.safeonline.beid;

import java.io.Serializable;

import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DeviceEntity;


public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    private DeviceEntity device;

    private AttributeDO  attribute;

    private boolean      disabled;

    private long         attributeIndex;


    public Registration(DeviceEntity device, AttributeDO attribute, boolean disabled, long attributeIndex) {

        this.device = device;
        this.attribute = attribute;
        this.disabled = disabled;
        this.attributeIndex = attributeIndex;
    }

    public DeviceEntity getDevice() {

        return this.device;
    }

    public AttributeDO getAttribute() {

        return this.attribute;
    }

    public boolean isDisabled() {

        return this.disabled;
    }

    public void setDisabled(boolean disabled) {

        this.disabled = disabled;
    }

    public long getAttributeIndex() {

        return this.attributeIndex;
    }
}
