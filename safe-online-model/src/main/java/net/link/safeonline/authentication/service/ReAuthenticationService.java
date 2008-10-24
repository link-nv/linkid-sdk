/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectMismatchException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Re-authentication service used by the user web application for account merging. This service lets a logged in user authenticate with the
 * available devices for another account. It stores all the authentication devices that have been authenticated with.
 * 
 * @author wvdhaute
 */
@Local
public interface ReAuthenticationService {

    /**
     * Returns the set of devices the user has authenticated successfully with.
     * 
     */
    Set<DeviceEntity> getAuthenticatedDevices();

    /**
     * Sets the source subject.
     * 
     * @param subject
     * @throws SubjectMismatchException
     * @throws PermissionDeniedException
     */
    void setAuthenticatedSubject(SubjectEntity subject) throws SubjectMismatchException, PermissionDeniedException;

    /**
     * Authenticates using a username-password device.
     * 
     * @param login
     * @param password
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     * @throws SubjectMismatchException
     * @throws PermissionDeniedException
     * @throws DeviceDisabledException
     */
    boolean authenticate(String login, String password) throws SubjectNotFoundException, DeviceNotFoundException, SubjectMismatchException,
                                                       PermissionDeniedException, DeviceDisabledException;

    /**
     * Aborts the current authentication procedure.
     */
    void abort();
}
