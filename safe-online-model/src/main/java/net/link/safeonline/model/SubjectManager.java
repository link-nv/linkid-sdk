package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.entity.SubjectEntity;

/**
 * Interface definition for subject manager component.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface SubjectManager {

	/**
	 * Gives back the subject entity corresponding with the SafeOnline core
	 * security domain caller principal.
	 * 
	 * @return
	 */
	SubjectEntity getCallerSubject();
}
