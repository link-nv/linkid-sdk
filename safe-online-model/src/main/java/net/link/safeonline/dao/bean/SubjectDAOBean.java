/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.entity.SubjectEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SubjectDAOBean implements SubjectDAO {

	private static final Log LOG = LogFactory.getLog(SubjectDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public SubjectEntity findSubject(String login) {
		LOG.debug("find subject: " + login);
		SubjectEntity subject = this.entityManager.find(SubjectEntity.class,
				login);
		return subject;
	}

	public SubjectEntity addSubject(String login) {
		LOG.debug("add subject: " + login);
		SubjectEntity subject = new SubjectEntity(login);
		this.entityManager.persist(subject);
		return subject;
	}

	public SubjectEntity getSubject(String login)
			throws SubjectNotFoundException {
		LOG.debug("get subject: " + login);
		SubjectEntity subject = findSubject(login);
		if (null == subject) {
			throw new SubjectNotFoundException();
		}
		return subject;
	}

	public void removeSubject(SubjectEntity subject) {
		this.entityManager.remove(subject);
	}
}
