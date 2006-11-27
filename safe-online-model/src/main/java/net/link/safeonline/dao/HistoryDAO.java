package net.link.safeonline.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.HistoryEntity;

@Local
public interface HistoryDAO {

	void addHistoryEntry(Date when, EntityEntity entity, String event);

	List<HistoryEntity> getHistory(EntityEntity entity);
}
