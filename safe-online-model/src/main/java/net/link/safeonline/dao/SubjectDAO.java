package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.entity.SubjectEntity;

/**
 * Subject Data Access Object interface.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubjectDAO {
	/**
	 * Finds the subject for a given login. Returns <code>null</code> if the
	 * entity could not be found.
	 * 
	 * @param login
	 * @return
	 */
	SubjectEntity findSubject(String login);

	SubjectEntity addSubject(String login, String password);

	SubjectEntity addSubject(String login, String password, String name);

	/**
	 * Gives back the subject for the given login.
	 * 
	 * @param login
	 *            the login of the entity.
	 * @return the subject.
	 * @exception SubjectNotFoundException
	 */
	SubjectEntity getSubject(String login) throws SubjectNotFoundException;
}
