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
}
