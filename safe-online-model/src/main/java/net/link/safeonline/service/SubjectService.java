/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Service bean that manages the mapping of the subject's userId with the login attribute
 * 
 * @author wvdhaute
 * 
 */

@Local
public interface SubjectService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/SubjectServiceBean/local";


    /**
     * Finds the subject for a given user ID. Returns <code>null</code> if the entity could not be found.
     * 
     * @param login
     * @return the subject or <code>null</code> if the subject was not found.
     */
    SubjectEntity findSubject(String userId);

    /**
     * Finds the subject for a given user login. Returns <code>null</code> if the entity could not be found.
     * 
     * @param login
     * @return the subject or <code>null</code> if the subject was not found.
     */
    SubjectEntity findSubjectFromUserName(String login);

    /**
     * Adds a subject for a given login. Generates a new UUID and adds the LOGIN attribute and SubjectIdentifier
     * 
     * @param login
     * @throws AttributeTypeNotFoundException
     */
    SubjectEntity addSubject(String login)
            throws AttributeTypeNotFoundException;

    /**
     * Adds a subject with the specified user ID. Does NOT add a login attribute and SubjectIdentifier. This method is used when the subject
     * is created from a NodeMapping ID.
     */
    SubjectEntity addSubjectWithoutLogin(String userId);

    /**
     * Gives back the subject for the given user ID.
     * 
     * @param login
     *            the login of the subject.
     * @return the subject.
     * @exception SubjectNotFoundException
     */
    SubjectEntity getSubject(String userId)
            throws SubjectNotFoundException;

    /**
     * Gives back the subject for the given user login name
     * 
     * @param login
     * @throws SubjectNotFoundException
     */
    SubjectEntity getSubjectFromUserName(String login)
            throws SubjectNotFoundException;

    /**
     * Returns the value of the login attribute associated with the given user ID. Returns <code>null</code> if not found.
     * 
     * @param userId
     */
    String getSubjectLogin(String userId);

    /**
     * Same as getSubjectLogin but this can be called from within exception handling code ( meaning a transaction is created )
     * 
     * @param userId
     */
    String getExceptionSubjectLogin(String userId);

    /**
     * Same as findSubject but this can be called from within exception handling code (meaning a transaction is created)
     * 
     * @param userId
     */
    SubjectEntity findExceptionSubject(String userId);

    /**
     * Returns list of users' login names starting with the specified prefix.
     * 
     * @param prefix
     * @throws AttributeTypeNotFoundException
     * 
     */
    List<String> listUsers(String prefix)
            throws AttributeTypeNotFoundException;
}
