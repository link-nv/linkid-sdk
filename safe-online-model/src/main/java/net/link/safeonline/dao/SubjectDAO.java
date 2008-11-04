/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;


/**
 * Subject Data Access Object interface.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubjectDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/SubjectDAOBean/local";


    /**
     * Finds the subject for a given user ID. Returns <code>null</code> if the entity could not be found.
     * 
     * @param userId
     * @return the subject or <code>null</code> if the subject was not found.
     */
    SubjectEntity findSubject(String userId);

    SubjectEntity addSubject(String userId);

    /**
     * Gives back the subject for the given userId.
     * 
     * @param userId
     *            the userId of the subject.
     * @return the subject.
     * @exception SubjectNotFoundException
     */
    SubjectEntity getSubject(String userId)
            throws SubjectNotFoundException;

    /**
     * Removes the given attached subject from the database.
     * 
     * @param subject
     */
    void removeSubject(SubjectEntity subject);

    /**
     * Gives back a list of all user Ids.
     * 
     */
    List<String> listUsers();
}
