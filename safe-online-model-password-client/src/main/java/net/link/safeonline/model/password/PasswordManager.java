/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface PasswordManager extends PasswordService {

    public static final String JNDI_BINDING = PasswordService.JNDI_PREFIX + "PasswordManagerBean/local";


    void registerPassword(SubjectEntity subject, String password);

    void updatePassword(SubjectEntity subject, String oldPassword, String newPassword)
            throws DeviceRegistrationNotFoundException, DeviceAuthenticationException;

    boolean validatePassword(SubjectEntity subject, String password)
            throws DeviceRegistrationNotFoundException;

    void removePassword(SubjectEntity subject);

    boolean isPasswordConfigured(SubjectEntity subject);
}
