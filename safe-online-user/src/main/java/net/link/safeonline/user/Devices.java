/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.DeviceMappingDO;


@Local
public interface Devices {

    /*
     * Accessors.
     */
    String getNewPassword();

    void setNewPassword(String newPassword);

    String getOldPassword();

    void setOldPassword(String oldPassword);

    boolean isPasswordConfigured() throws SubjectNotFoundException, DeviceNotFoundException;

    /*
     * Actions.
     */
    String register() throws DeviceNotFoundException, IOException;

    String remove() throws DeviceNotFoundException, IOException;

    String removeDevice() throws DeviceNotFoundException, IOException;

    String update() throws DeviceNotFoundException, IOException;

    String updateDevice() throws DeviceNotFoundException, IOException;

    String changePassword() throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException;

    String registerPassword() throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException;

    String removePassword() throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException;

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Factory.
     */
    List<DeviceEntry> devicesFactory() throws SubjectNotFoundException, DeviceNotFoundException;

    List<DeviceMappingDO> deviceRegistrationsFactory() throws SubjectNotFoundException, DeviceNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException;
}
