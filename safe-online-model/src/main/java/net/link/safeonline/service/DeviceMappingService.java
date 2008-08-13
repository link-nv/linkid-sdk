/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceMappingNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * <h2>{@link DeviceMappingService} - Service for device mapping registration.</h2>
 *
 * <p>
 * Creates device mappings for subject-device issuer pair. These mappings contain a UUID that is used by the device
 * provider to map the identity provided by their device to an OLAS identity.
 * </p>
 *
 * <p>
 * <i>Jan 29, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface DeviceMappingService {

    /**
     * Get or create if not existing a device mapping for the specified user and device. This device mapping will be
     * used to communicate with the external device issuer.
     *
     * @param userId
     * @param deviceName
     * @return the device mapping
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     */
    public DeviceMappingEntity getDeviceMapping(String userId, String deviceName) throws SubjectNotFoundException,
            DeviceNotFoundException;

    public DeviceMappingEntity getDeviceMapping(String id) throws DeviceMappingNotFoundException;

    public List<DeviceMappingEntity> listDeviceMappings(SubjectEntity subject);
}
