/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.lin_k.safe_online.identity_statement._1.IdentityDataType;
import net.lin_k.safe_online.identity_statement._1.IdentityStatementType;
import net.lin_k.safe_online.identity_statement._1.ObjectFactory;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class CredentialServiceBean implements CredentialService {

	private static Log LOG = LogFactory.getLog(CredentialServiceBean.class);

	@EJB
	private SubjectManager subjectManager;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void changePassword(String oldPassword, String newPassword)
			throws PermissionDeniedException {
		LOG.debug("change password");
		String login = this.subjectManager.getCallerLogin();

		AttributeEntity passwordAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login);
		if (null == passwordAttribute) {
			throw new EJBException(
					"password attribute not present for subject: " + login);
		}

		String currentPassword = passwordAttribute.getStringValue();
		if (null == currentPassword) {
			throw new EJBException("current password is null");
		}

		if (!currentPassword.equals(oldPassword)) {
			throw new PermissionDeniedException();
		}

		passwordAttribute.setStringValue(newPassword);

		SecurityManagerUtils.flushCredentialCache(login,
				SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void mergeIdentityStatement(String identityStatementStr) {
		LOG.debug("merge identity statement");
		String login = this.subjectManager.getCallerLogin();
		LOG.debug("login: " + login);
		Document identityStatementDocument = verifyIntegrity(identityStatementStr);
		IdentityStatementType identityStatement = parseIdentityStatement(identityStatementDocument);
		// TODO: have a separate module for BeID
		IdentityDataType identityData = identityStatement.getIdentityData();
		String surname = identityData.getSurname();
		String givenName = identityData.getGivenName();

		setOrOverrideAttribute(SafeOnlineConstants.SURNAME_ATTRIBUTE, login,
				surname);
		setOrOverrideAttribute(SafeOnlineConstants.GIVENNAME_ATTRIBUTE, login,
				givenName);
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

	private Document getDocument(String documentStr) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(
					"parser config error: " + e.getMessage(), e);
		}
		InputSource inputSource = new InputSource(new StringReader(documentStr));
		try {
			Document document = documentBuilder.parse(inputSource);
			return document;
		} catch (SAXException e) {
			throw new IllegalArgumentException("identity statement error: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
	}

	private Document verifyIntegrity(String identityStatementStr) {
		Document document = getDocument(identityStatementStr);

		NodeList signatureNodeList = document.getElementsByTagNameNS(
				XMLSignature.XMLNS, "Signature");
		if (0 == signatureNodeList.getLength()) {
			String msg = "no signature found in identity statement";
			LOG.debug(msg);
			throw new IllegalArgumentException(msg);
		}

		DOMValidateContext validateContext = new DOMValidateContext(
				new LocalKeySelector(), signatureNodeList.item(0));
		XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature;
		try {
			signature = factory.unmarshalXMLSignature(validateContext);
		} catch (MarshalException e) {
			throw new IllegalArgumentException("marshal error: "
					+ e.getMessage());
		}
		try {
			if (false == signature.validate(validateContext)) {
				throw new IllegalArgumentException("invalid signature");
			}
		} catch (XMLSignatureException e) {
			throw new IllegalArgumentException("XML signature error: "
					+ e.getMessage());
		}

		X509Certificate certificate = findX509Certificate(signature
				.getKeyInfo());
		KeySelectorResult keySelectorResult = signature.getKeySelectorResult();
		if (false == keySelectorResult.getKey().equals(
				certificate.getPublicKey())) {
			throw new RuntimeException(
					"verification key should correspond with the certificate");
		}

		TrustDomainEntity trustDomain = this.pkiProviderManager
				.findTrustDomain(certificate);
		if (null == trustDomain) {
			throw new IllegalArgumentException(
					"no appropriate PKI provider found");
		}

		boolean verificationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == verificationResult) {
			throw new IllegalArgumentException("invalid certificate");
		}

		// TODO: check that the correct document node has been signed

		return document;
	}

	@SuppressWarnings("unchecked")
	private static X509Certificate findX509Certificate(KeyInfo keyInfo) {
		List<XMLStructure> keyInfoContent = keyInfo.getContent();
		for (XMLStructure keyInfoXmlStructure : keyInfoContent) {
			if (keyInfoXmlStructure instanceof X509Data) {
				X509Data x509Data = (X509Data) keyInfoXmlStructure;
				List<Object> x509DataContent = x509Data.getContent();
				for (Object x509DataObject : x509DataContent) {
					if (x509DataObject instanceof X509Certificate) {
						X509Certificate certificate = (X509Certificate) x509DataObject;
						return certificate;
					}
				}
			}
		}
		return null;
	}

	private static class LocalKeySelector extends KeySelector {

		private static final Log LOG = LogFactory
				.getLog(LocalKeySelector.class);

		@Override
		public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
				AlgorithmMethod method, XMLCryptoContext context)
				throws KeySelectorException {
			LOG.debug("select key");
			if (null == keyInfo) {
				throw new KeySelectorException("Null KeyInfo object!");
			}
			X509Certificate certificate = findX509Certificate(keyInfo);
			if (null == certificate) {
				throw new KeySelectorException(
						"no appropriate key info entry found");
			}
			LOG
					.debug("key info cert: "
							+ certificate.getSubjectX500Principal());
			KeySelectorResult result = new LocalKeySelectorResult(certificate
					.getPublicKey());
			return result;

		}
	}

	private static class LocalKeySelectorResult implements KeySelectorResult {

		private final PublicKey publicKey;

		public LocalKeySelectorResult(PublicKey publicKey) {
			this.publicKey = publicKey;
		}

		public Key getKey() {
			return this.publicKey;
		}
	}

	@SuppressWarnings("unchecked")
	private IdentityStatementType parseIdentityStatement(
			Document identityStatementDocument) {
		try {
			JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			JAXBElement<IdentityStatementType> identityStatementElement = (JAXBElement<IdentityStatementType>) unmarshaller
					.unmarshal(identityStatementDocument);
			return identityStatementElement.getValue();
		} catch (JAXBException e) {
			throw new IllegalArgumentException(
					"count not parse the identity statement");
		}
	}
}
