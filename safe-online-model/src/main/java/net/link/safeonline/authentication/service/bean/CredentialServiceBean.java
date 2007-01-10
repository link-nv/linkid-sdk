/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.io.IOException;
import java.io.StringReader;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.util.ee.SecurityManagerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;
import org.w3c.dom.Document;
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

	private Document verifyIntegrity(String identityStatementStr) {
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
		InputSource inputSource = new InputSource(new StringReader(
				identityStatementStr));
		Document document;
		try {
			document = documentBuilder.parse(inputSource);
		} catch (SAXException e) {
			throw new IllegalArgumentException("identity statement error: "
					+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("IO error: " + e.getMessage(), e);
		}
		// TODO: verify integrity (need PKI component for this)
		return document;
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
