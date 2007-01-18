/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.TrustPointNotFoundException;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.entity.TrustPointPK;

@Stateless
public class TrustPointDAOBean implements TrustPointDAO {

	private static final Log LOG = LogFactory.getLog(TrustPointDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public void addTrustPoint(TrustDomainEntity trustDomain,
			X509Certificate certificate) {
		LOG.debug("add trust point to domain: " + trustDomain.getName()
				+ " with subject " + certificate.getSubjectX500Principal());
		TrustPointEntity trustPoint = new TrustPointEntity(trustDomain,
				certificate);
		this.entityManager.persist(trustPoint);
	}

	@SuppressWarnings("unchecked")
	public List<TrustPointEntity> getTrustPoints(TrustDomainEntity trustDomain) {
		LOG.debug("get trust points for domain: " + trustDomain.getName());
		Query query = TrustPointEntity.createQueryWhereDomain(
				this.entityManager, trustDomain);
		List<TrustPointEntity> trustPoints = query.getResultList();
		return trustPoints;
	}

	public TrustPointEntity getTrustPoint(TrustDomainEntity trustDomain,
			String subjectName) throws TrustPointNotFoundException {
		LOG.debug("get trust point for domain " + trustDomain.getName()
				+ " and subject name " + subjectName);
		TrustPointPK pk = new TrustPointPK(trustDomain, subjectName);
		TrustPointEntity trustPoint = this.entityManager.find(
				TrustPointEntity.class, pk);
		if (null == trustPoint) {
			throw new TrustPointNotFoundException();
		}
		return trustPoint;
	}

	public void removeTrustPoint(TrustPointEntity trustPoint) {
		LOG.debug("remove trust point: " + trustPoint);
		this.entityManager.remove(trustPoint);
	}

	public TrustPointEntity findTrustPoint(TrustDomainEntity trustDomain,
			String subjectName) {
		LOG.debug("find trust point for domain " + trustDomain.getName()
				+ " and subject name " + subjectName);
		TrustPointPK pk = new TrustPointPK(trustDomain, subjectName);
		TrustPointEntity trustPoint = this.entityManager.find(
				TrustPointEntity.class, pk);
		return trustPoint;
	}
}
