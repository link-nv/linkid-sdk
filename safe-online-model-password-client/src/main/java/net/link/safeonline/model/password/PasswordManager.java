/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.password;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface PasswordManager extends PasswordService {

    public static final String JNDI_BINDING = PasswordService.JNDI_PREFIX + "PasswordManagerBean/local";


    void setPassword(SubjectEntity subject, String password)
            throws PermissionDeniedException;

    void changePassword(SubjectEntity subject, String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException;

    boolean validatePassword(SubjectEntity subject, String password)
            throws DeviceNotFoundException;

    boolean isPasswordConfigured(SubjectEntity subject);

    void removePassword(SubjectEntity subject, String password)
            throws DeviceNotFoundException, PermissionDeniedException;

    boolean isDisabled(SubjectEntity subject)
            throws DeviceNotFoundException;

    /**
     * @param subject
     * @param disable
     * 
     * @throws DeviceNotFoundException
     */
    void disablePassword(SubjectEntity subject, boolean disable)
            throws DeviceNotFoundException;

}
