/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingService;


/**
 * Interface for identifier mapping service component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface ApplicationIdentifierMappingService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "ApplicationIdentifierMappingServiceBean/local";


    /**
     * Returns the subject's user ID using the application's id scope. This method will check if the application has permission to access
     * the {@link NameIdentifierMappingService}.
     * 
     * @param username
     * @throws PermissionDeniedException
     * @throws ApplicationNotFoundException
     * @throws SubjectNotFoundException
     */
    String getApplicationUserId(String username)
            throws PermissionDeniedException, ApplicationNotFoundException, SubjectNotFoundException;

    /**
     * Returns the subject's user ID using the application's id scope. This method will <b>NOT</b> check if the application has permission
     * to access {@link NameIdentifierMappingService}.
     */
    String getApplicationUserId(SubjectEntity subject);

    /**
     * Returns the global OLAS user ID using the application's id scope. Returns null if not found.
     * 
     * @param applicationId
     * @param applicationUserId
     * @throws ApplicationNotFoundException
     */
    String findUserId(long applicationId, String applicationUserId)
            throws ApplicationNotFoundException;

    /**
     * Returns the global OLAS user ID using the application's id scope. Returns null if not found.
     * 
     * @param application
     * @param applicationUserId
     */
    String findUserId(ApplicationEntity application, String applicationUserId);
}
