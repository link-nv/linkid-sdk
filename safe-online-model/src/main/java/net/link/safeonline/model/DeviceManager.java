/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.entity.DeviceEntity;


/**
 * Interface for the device manager component.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface DeviceManager {

    /**
     * Gives back the caller device. Calling this method only makes sense in the context of a device login (via a device
     * web service).
     * 
     */
    DeviceEntity getCallerDevice();
}
