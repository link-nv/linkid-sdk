/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface PasswordDeviceService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "PasswordDeviceServiceBean/local";


    SubjectEntity authenticate(String loginName, String password)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceDisabledException;

    void register(String userId, String password)
            throws SubjectNotFoundException, DeviceNotFoundException;

    void register(SubjectEntity subject, String password)
            throws SubjectNotFoundException, DeviceNotFoundException;

    void update(SubjectEntity subject, String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException;

    void remove(SubjectEntity subject, String password)
            throws DeviceNotFoundException, PermissionDeniedException, SubjectNotFoundException;

    boolean isPasswordConfigured(SubjectEntity subject)
            throws SubjectNotFoundException, DeviceNotFoundException;

    void disable(SubjectEntity subject)
            throws DeviceNotFoundException;

}
