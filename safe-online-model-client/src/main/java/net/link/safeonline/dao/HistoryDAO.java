/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;


/**
 * History entity data access object interface definition.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface HistoryDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "HistoryDAOBean/local";


    HistoryEntity addHistoryEntry(Date when, SubjectEntity subject, HistoryEventType event, Map<String, String> properties);

    HistoryEntity addHistoryEntry(SubjectEntity subject, HistoryEventType event, Map<String, String> properties);

    /**
     * Gives back all history entries for a given subject.
     * 
     * @param subject
     * @return the list of history entries, or an empty list in case no history entries exist yet.
     */
    List<HistoryEntity> getHistory(SubjectEntity subject);

    /**
     * Deletes all history entries older than a given age limit
     * 
     * @param ageInMillis
     */
    void clearAllHistory(Date ageLimit);

    /**
     * Deletes all history entries of the given user.
     */
    void clearAllHistory(SubjectEntity subject);

    /**
     * This method will have its own transaction, only to be used in case like in an exception handler, ... We dont need a seperate
     * transaction for every history entry by default
     * 
     * @param when
     * @param subject
     * @param event
     * @param properties
     */
    HistoryEntity addHExceptionHistoryEntry(Date when, SubjectEntity subject, HistoryEventType event, Map<String, String> properties);
}