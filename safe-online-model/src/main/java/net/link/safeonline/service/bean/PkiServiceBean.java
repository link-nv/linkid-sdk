/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.CertificateEncodingException;
import net.link.safeonline.authentication.exception.ExistingTrustDomainException;
import net.link.safeonline.authentication.exception.ExistingTrustPointException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.exception.TrustPointNotFoundException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;
import net.link.safeonline.service.PkiService;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class PkiServiceBean implements PkiService {

	private static final Log LOG = LogFactory.getLog(PkiServiceBean.class);

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TrustDomainEntity> getTrustDomains() {
		List<TrustDomainEntity> trustDomains = this.trustDomainDAO
				.getTrustDomains();
		return trustDomains;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addTrustDomain(String name) throws ExistingTrustDomainException {
		TrustDomainEntity existingTrustDomain = this.trustDomainDAO
				.findTrustDomain(name);
		if (null != existingTrustDomain) {
			throw new ExistingTrustDomainException();
		}
		this.trustDomainDAO.addTrustDomain(name);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeTrustDomain(String name)
			throws TrustDomainNotFoundException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(name);
		List<TrustPointEntity> trustPoints = this.trustPointDAO
				.getTrustPoints(trustDomain);
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
		X509Certificate certificate = decodeCertificate(encodedCertificate);
		String subjectName = certificate.getSubjectX500Principal().toString();
		LOG.debug("subject name: " + subjectName);
		TrustPointEntity existingTrustPoint = this.trustPointDAO
				.findTrustPoint(trustDomain, subjectName);
		if (null != existingTrustPoint) {
			throw new ExistingTrustPointException();
		}
		// TODO: validate the certificate first
		this.trustPointDAO.addTrustPoint(trustDomain, certificate);
	}

	private X509Certificate decodeCertificate(byte[] encodedCertificate)
			throws CertificateEncodingException {
		CertificateFactory certificateFactory;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			throw new EJBException("certificate factory error: "
					+ e.getMessage());
		}
		InputStream certInputStream = new ByteArrayInputStream(
				encodedCertificate);
		try {
			X509Certificate certificate = (X509Certificate) certificateFactory
					.generateCertificate(certInputStream);
			return certificate;
		} catch (CertificateException e) {
			throw new CertificateEncodingException();
		}
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<TrustPointEntity> getTrustPoints(String domainName)
			throws TrustDomainNotFoundException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(domainName);
		List<TrustPointEntity> trustPoints = this.trustPointDAO
				.getTrustPoints(trustDomain);
		return trustPoints;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeTrustPoint(TrustDomainEntity trustDomain,
			String subjectName) throws TrustPointNotFoundException {
		TrustPointEntity trustPoint = this.trustPointDAO.getTrustPoint(
				trustDomain, subjectName);
		this.trustPointDAO.removeTrustPoint(trustPoint);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public TrustDomainEntity getTrustDomain(String trustDomainName)
			throws TrustDomainNotFoundException {
		LOG.debug("get trust domain: " + trustDomainName);
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(trustDomainName);
		return trustDomain;
	}
}
