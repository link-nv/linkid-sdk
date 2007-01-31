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
}
