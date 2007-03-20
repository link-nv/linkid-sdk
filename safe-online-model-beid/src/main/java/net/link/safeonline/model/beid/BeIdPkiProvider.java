/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.security.auth.x500.X500Principal;

import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.bean.IdentityStatementAttributes;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.PkiProvider;
import net.link.safeonline.model.SubjectManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = PkiProvider.PKI_PROVIDER_JNDI + "/beid")
public class BeIdPkiProvider implements PkiProvider {

	public static final String TRUST_DOMAIN_NAME = "beid";

	public static final String IDENTIFIER_DOMAIN_NAME = "beid";

	private static final Log LOG = LogFactory.getLog(BeIdPkiProvider.class);

	@Resource
	private SessionContext context;

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private SubjectManager subjectManager;

	public boolean accept(X509Certificate certificate) {
		X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		String subject = subjectPrincipal.toString();
		LOG.debug("subject: " + subject);
		if (subject.indexOf("SERIALNUMBER") == -1) {
			return false;
		}
		if (subject.indexOf("GIVENNAME") == -1) {
			return false;
		}
		if (subject.indexOf("SURNAME") == -1) {
			return false;
		}
		return true;
	}

	public TrustDomainEntity getTrustDomain()
			throws TrustDomainNotFoundException {
		TrustDomainEntity trustDomain = this.trustDomainDAO
				.getTrustDomain(TRUST_DOMAIN_NAME);
		return trustDomain;
	}

	public PkiProvider getReference() {
		LOG.debug("get reference");
		PkiProvider reference = (PkiProvider) this.context
				.getBusinessObject(PkiProvider.class);
		return reference;
	}

	public String mapAttribute(
			IdentityStatementAttributes identityStatementAttributes) {
		switch (identityStatementAttributes) {
		case SURNAME:
			return BeIdConstants.SURNAME_ATTRIBUTE;
		case GIVEN_NAME:
			return BeIdConstants.GIVENNAME_ATTRIBUTE;
		case AUTH_CERT:
			return BeIdConstants.AUTH_CERT_ATTRIBUTE;
		default:
			throw new IllegalArgumentException(
					"unsupported identity statement attribute");
		}
	}

	public String getIdentifierDomainName() {
		return IDENTIFIER_DOMAIN_NAME;
	}

	public String getSubjectIdentifier(X509Certificate certificate) {
		byte[] data;
		try {
			data = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new IllegalArgumentException("cert encoding error: "
					+ e.getMessage());
		}
		String identifier = DigestUtils.shaHex(data);
		return identifier;
	}

	public void storeAdditionalAttributes(X509Certificate certificate) {
		String subjectName = getSubjectName(certificate);
		String nrn = getAttributeFromSubjectName(subjectName, "SERIALNUMBER");
		String login = this.subjectManager.getCallerLogin();
		setOrOverrideAttribute(BeIdConstants.NRN_ATTRIBUTE, login, nrn);
	}

	private void setOrOverrideAttribute(String attributeName, String login,
			String value) {
		AttributeEntity attribute = this.attributeDAO.findAttribute(
				attributeName, login);
		if (null == attribute) {
			this.attributeDAO.addAttribute(attributeName, login, value);
		} else {
			attribute.setStringValue(value);
		}
	}

	private String getSubjectName(X509Certificate certificate) {
		X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		String subjectName = subjectPrincipal.toString();
		return subjectName;
	}

	private String getAttributeFromSubjectName(String subjectName,
			String attributeName) {
		int attributeBegin = subjectName.indexOf(attributeName + "=");
		if (-1 == attributeBegin) {
			throw new IllegalArgumentException(
					"attribute name does not occur in subject: "
							+ attributeName);
		}
		attributeBegin += attributeName.length() + 1; // "attributeName="
		int attributeEnd = subjectName.indexOf(",", attributeBegin);
		if (-1 == attributeEnd) {
			// last field has no trailing ","
			attributeEnd = subjectName.length();
		}
		String attributeValue = subjectName.substring(attributeBegin,
				attributeEnd);
		return attributeValue;
	}
}
