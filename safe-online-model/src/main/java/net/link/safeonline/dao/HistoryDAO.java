/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;

/**
 * History entity data access object interface definition.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface HistoryDAO {

	void addHistoryEntry(Date when, SubjectEntity subject, String event);

	/**
	 * Gives back all history entries for a given subject.
	 * 
	 * @param subject
	 * @return the list of history entries, or an empty list in case no history
	 *         entries exist yet.
	 */
	List<HistoryEntity> getHistory(SubjectEntity subject);

	/**
	 * Deletes all history entries older than a given age in milliseconds
	 * 
	 * @param ageInMillis
	 */
	void clearAllHistory(long ageInMillis);
}
