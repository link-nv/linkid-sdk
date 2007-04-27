/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.SubjectEntity;

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
}
