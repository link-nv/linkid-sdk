/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface PasswordDeviceService extends PasswordService {

    public static final String JNDI_BINDING = PasswordService.JNDI_PREFIX + "PasswordDeviceServiceBean/local";


    String authenticate(String userId, String password)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceDisabledException;

    void register(String nodeName, String userId, String password)
            throws NodeNotFoundException;

    void update(String userId, String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException;

    void remove(String userId)
            throws DeviceNotFoundException, SubjectNotFoundException;

    void disable(String userId)
            throws DeviceNotFoundException, SubjectNotFoundException;

    void enable(String userId, String password)
            throws DeviceNotFoundException, SubjectNotFoundException, PermissionDeniedException;

    boolean isPasswordConfigured(String userId)
            throws SubjectNotFoundException;

}
