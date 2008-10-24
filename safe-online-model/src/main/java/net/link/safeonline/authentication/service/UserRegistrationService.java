/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;


/**
 * User registration service interface.
 * 
 * The component implementing this interface will allow for registration of new users within the SafeOnline core. This means creating a new
 * Subject and subscribing the new Subject to the safe-online-user application.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface UserRegistrationService {

    /**
     * Checks whether the given login name already exists and has completed device registrations. In case there are existing device
     * registrations it will poll those device issuers if the registration actually completed. If one them has completed, return null.
     * 
     * If no such login exists, register and return the subject.
     * 
     * If no completed device registrations were found, return the subject.
     * 
     * @param login
     * @throws AttributeTypeNotFoundException
     * @throws ExistingUserException
     * @throws PermissionDeniedException
     * @throws AttributeUnavailableException
     */
    SubjectEntity registerUser(String login) throws ExistingUserException, AttributeTypeNotFoundException, PermissionDeniedException,
                                            AttributeUnavailableException;
}
