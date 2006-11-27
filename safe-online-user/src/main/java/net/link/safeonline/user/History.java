package net.link.safeonline.user;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.HistoryEntity;

@Local
public interface History {

	List<HistoryEntity> getList();
}
