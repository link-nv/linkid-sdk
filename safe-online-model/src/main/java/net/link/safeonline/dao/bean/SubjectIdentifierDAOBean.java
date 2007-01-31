/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubjectIdentifierEntity;
import net.link.safeonline.entity.SubjectIdentifierPK;

@Stateless
public class SubjectIdentifierDAOBean implements SubjectIdentifierDAO {

	private static final Log LOG = LogFactory
			.getLog(SubjectIdentifierDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addSubjectIdentifier(String domain, String subjectIdentifier,
			SubjectEntity subject) {
		LOG.debug("add subject identifier");
		SubjectIdentifierEntity subjectIdentifierEntity = new SubjectIdentifierEntity(
				domain, subjectIdentifier, subject);
		this.entityManager.persist(subjectIdentifierEntity);
	}

	public SubjectEntity findSubject(String domain, String subjectIdentifier) {
		SubjectIdentifierPK pk = new SubjectIdentifierPK(domain,
				subjectIdentifier);
		SubjectIdentifierEntity subjectIdentifierEntity = this.entityManager
				.find(SubjectIdentifierEntity.class, pk);
		if (null == subjectIdentifierEntity) {
			return null;
		}
		SubjectEntity subject = subjectIdentifierEntity.getSubject();
		return subject;
	}
}