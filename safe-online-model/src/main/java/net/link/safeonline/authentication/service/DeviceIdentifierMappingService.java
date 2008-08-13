/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Interface for device identifier mapping service component.
 * 
 * @author wvdhaute
 * 
 */
@Local
public interface DeviceIdentifierMappingService {

    /**
     * Returns the device mapping id for the specified user and authenticating remote device.
     * 
     * @param username
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     */
    String getDeviceMappingId(String username) throws DeviceNotFoundException, SubjectNotFoundException;
}
