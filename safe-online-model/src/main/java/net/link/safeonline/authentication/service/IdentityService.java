package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.EntityNotFoundException;
import net.link.safeonline.entity.HistoryEntity;

@Local
public interface IdentityService {

	String getName(String login) throws EntityNotFoundException;

	void saveName(String login, String name) throws EntityNotFoundException;

	List<HistoryEntity> getHistory(String login) throws EntityNotFoundException;
}
