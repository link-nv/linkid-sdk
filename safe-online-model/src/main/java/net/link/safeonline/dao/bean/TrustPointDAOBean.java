/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao.bean;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

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
			String subjectName, String keyId)
			throws TrustPointNotFoundException {
		LOG.debug("get trust point for domain " + trustDomain.getName()
				+ " and subject name " + subjectName);
		TrustPointPK pk = new TrustPointPK(trustDomain, subjectName, keyId);
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
			String subjectName, String keyId) {
		LOG.debug("find trust point for domain " + trustDomain.getName()
				+ " and subject name " + subjectName);
		TrustPointPK pk = new TrustPointPK(trustDomain, subjectName, keyId);
		TrustPointEntity trustPoint = this.entityManager.find(
				TrustPointEntity.class, pk);
		return trustPoint;
	}

	public String getSubjectKeyId(X509Certificate certificate) {
		byte[] subjectKeyIdData = certificate
				.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
		if (null == subjectKeyIdData) {
			throw new EJBException(
					"certificate has no subject key identifier extension");
		}
		SubjectKeyIdentifierStructure subjectKeyIdentifierStructure;
		try {
			subjectKeyIdentifierStructure = new SubjectKeyIdentifierStructure(
					subjectKeyIdData);
		} catch (IOException e) {
			throw new EJBException(
					"error parsing the subject key identifier certificate extension");
		}
		String keyId = new String(Hex.encodeHex(subjectKeyIdentifierStructure
				.getKeyIdentifier()));
		return keyId;
	}

	public TrustPointEntity getTrustPoint(TrustPointPK pk)
			throws TrustPointNotFoundException {
		TrustPointEntity trustPoint = this.entityManager.find(
				TrustPointEntity.class, pk);
		if (null == trustPoint) {
			throw new TrustPointNotFoundException();
		}
		return trustPoint;
	}
}
