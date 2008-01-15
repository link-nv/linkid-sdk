/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;

@Local
public interface SubjectIdentifierDAO {

	void addSubjectIdentifier(String domain, String subjectIdentifier,
			SubjectEntity subject);

	SubjectEntity findSubject(String domain, String subjectIdentifier);

	/**
	 * Removes subject identifiers within the given domain for the given user
	 * that have a different identifier than the given identifier.
	 * 
	 * @param domain
	 * @param identifier
	 * @param subject
	 */
	void removeOtherSubjectIdentifiers(String domain, String identifier,
			SubjectEntity subject);

	/**
	 * Removes all the subject identifiers for the given subject.
	 * 
	 * @param subject
	 */
	void removeSubjectIdentifiers(SubjectEntity subject);

	/**
	 * Remove specified subject identifier.
	 * 
	 * @param domain
	 * @param identifier
	 */
	void removeSubjectIdentifier(SubjectEntity subject, String domain,
			String identifier);

	/**
	 * Returns list of subject identifiers for the given subject.
	 * 
	 * @param subject
	 */
	List<SubjectIdentifierEntity> getSubjectIdentifiers(SubjectEntity subject);
}
