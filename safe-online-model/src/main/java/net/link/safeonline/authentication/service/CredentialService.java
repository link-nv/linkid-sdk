/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


/**
 * Interface of service that manages the credentials of the caller subject.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface CredentialService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/CredentialServiceBean/local";


    /**
     * Changes the password of the current user. Of course for that to happen the oldPassword must match.
     * 
     * @param oldPassword
     * @param newPassword
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     */
    void changePassword(String oldPassword, String newPassword)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException;

    /**
     * Register password for the current user.
     * 
     * @param password
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     */
    void registerPassword(String password)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException;

    /**
     * Removes the password of the current user. For this to happen the password must match.
     * 
     * @param password
     * @throws DeviceNotFoundException
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     */
    void removePassword(String password)
            throws DeviceNotFoundException, PermissionDeniedException, SubjectNotFoundException;

    /**
     * Gives back <code>true</code> if the user already has a password configured.
     * 
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     * 
     */
    boolean isPasswordConfigured()
            throws SubjectNotFoundException, DeviceNotFoundException;

    /**
     * Disables/enables the password registration of the current user.
     * 
     * @throws DeviceNotFoundException
     */
    void disablePassword()
            throws DeviceNotFoundException;
}
