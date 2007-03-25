/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingTrustDomainException;
import net.link.safeonline.authentication.exception.ExistingTrustPointException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.exception.TrustPointNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.CachedOcspResponseDAO;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.model.PkiUtils;
import net.link.safeonline.service.PkiService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class PkiServiceBean implements PkiService {

	private static final Log LOG = LogFactory.getLog(PkiServiceBean.class);

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	@EJB
	private CachedOcspResponseDAO cachedOcspResponseDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TrustDomainEntity> listTrustDomains() {
		List<TrustDomainEntity> trustDomains = this.trustDomainDAO
				.listTrustDomains();
		return trustDomains;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addTrustDomain(String name, boolean performOcspCheck)
			throws ExistingTrustDomainException {
		TrustDomainEntity existingTrustDomain = this.trustDomainDAO
				.findTrustDomain(name);
		if (null != existingTrustDomain) {
			throw new ExistingTrustDomainException();
		}
		this.trustDomainDAO.addTrustDomain(name, performOcspCheck);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addTrustDomain(String name, boolean performOcspCheck,
			long ocspCacheTimeOutMillis) throws ExistingTrustDomainException {
		TrustDomainEntity existingTrustDomain = this.trustDomainDAO
				.findTrustDomain(name);
		if (null != existingTrustDomain) {
			throw new ExistingTrustDomainException();
		}
		this.trustDomainDAO.addTrustDomain(name, performOcspCheck,
				ocspCacheTimeOutMillis);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeTrustDomain(String name)
			throws TrustDomainNotFoundException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(name);
		List<TrustPointEntity> trustPoints = this.trustPointDAO
				.listTrustPoints(trustDomain);
		for (TrustPointEntity trustPoint : trustPoints) {
			this.trustPointDAO.removeTrustPoint(trustPoint);
		}
		this.trustDomainDAO.removeTrustDomain(trustDomain);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addTrustPoint(String domainName, byte[] encodedCertificate)
			throws TrustDomainNotFoundException, CertificateEncodingException,
			ExistingTrustPointException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(domainName);
		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);
		String subjectName = certificate.getSubjectX500Principal().toString();
		LOG.debug("subject name: " + subjectName);
		TrustPointEntity existingTrustPoint = this.trustPointDAO
				.findTrustPoint(trustDomain, certificate);
		if (null != existingTrustPoint) {
			throw new ExistingTrustPointException();
		}
		this.trustPointDAO.addTrustPoint(trustDomain, certificate);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TrustPointEntity> listTrustPoints(String domainName)
			throws TrustDomainNotFoundException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(domainName);
		List<TrustPointEntity> trustPoints = this.trustPointDAO
				.listTrustPoints(trustDomain);
		return trustPoints;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeTrustPoint(TrustPointEntity trustPoint)
			throws TrustPointNotFoundException {
		TrustPointEntity attachedTrustPoint = this.trustPointDAO
				.getTrustPoint(trustPoint.getPk());
		this.trustPointDAO.removeTrustPoint(attachedTrustPoint);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public TrustDomainEntity getTrustDomain(String trustDomainName)
			throws TrustDomainNotFoundException {
		LOG.debug("get trust domain: " + trustDomainName);
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(trustDomainName);
		return trustDomain;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void saveTrustDomain(TrustDomainEntity trustDomain)
			throws TrustDomainNotFoundException {
		LOG.debug("save trust domain: " + trustDomain);
		// TODO: use EntityManager.merge instead
		TrustDomainEntity attachedTrustDomain = this.trustDomainDAO
				.getTrustDomain(trustDomain.getName());
		attachedTrustDomain.setPerformOcspCheck(trustDomain
				.isPerformOcspCheck());
		attachedTrustDomain.setOcspCacheTimeOutMillis(trustDomain
				.getOcspCacheTimeOutMillis());
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void clearOcspCache() {
		this.cachedOcspResponseDAO.clearOcspCache();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void clearOcspCachePerTrustDomain(TrustDomainEntity trustDomain) {
		this.cachedOcspResponseDAO.clearOcspCachePerTrustDomain(trustDomain);
	}
}
