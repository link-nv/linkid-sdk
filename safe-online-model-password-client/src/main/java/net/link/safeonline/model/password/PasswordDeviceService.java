/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface PasswordDeviceService extends PasswordService {

    public static final String JNDI_BINDING = PasswordService.JNDI_PREFIX + "PasswordDeviceServiceBean/local";


    void authenticate(String userId, String password)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException;

    void register(String nodeName, String userId, String password)
            throws NodeNotFoundException;

    void update(String userId, String oldPassword, String newPassword)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException;

    void remove(String userId)
            throws SubjectNotFoundException;

    void enable(String userId, String password)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceAuthenticationException;

    void disable(String userId)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    boolean isPasswordConfigured(String subject)
            throws SubjectNotFoundException;
}
