/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.bean;

import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.ResourceBundle;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.mandate.AuthorizationService;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.demo.mandate.entity.UserEntity;
import net.link.safeonline.demo.mandate.keystore.DemoMandateKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = AuthorizationService.JNDI_BINDING)
public class AuthorizationServiceBean implements AuthorizationService {

	private final String WEBSERVICE_CONFIG = "ws_config";

	private static final Log LOG = LogFactory
			.getLog(AuthorizationServiceBean.class);

	@PersistenceContext(unitName = MandateConstants.ENTITY_MANAGER_NAME)
	private EntityManager entityManager;

	private String getUsername(String userId) {
		String username;
		AttributeClient attributeClient = getAttributeClient();
		try {
			username = attributeClient.getAttributeValue(userId,
					DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
		} catch (ConnectException e) {
			LOG.debug("connection error: " + e.getMessage());
			return null;
		} catch (RequestDeniedException e) {
			LOG.debug("request denied");
			return null;
		} catch (AttributeNotFoundException e) {
			LOG.debug("login attribute not found");
			return null;
		}

		LOG.debug("username = " + username);
		return username;

	}

	private AttributeClient getAttributeClient() {
		ResourceBundle config = ResourceBundle
				.getBundle(this.WEBSERVICE_CONFIG);
		String wsLocation = config.getString("WsLocation");

		LOG.debug("Webservice: " + wsLocation);

		PrivateKeyEntry privateKeyEntry = DemoMandateKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate certificate = (X509Certificate) privateKeyEntry
				.getCertificate();
		PrivateKey privateKey = privateKeyEntry.getPrivateKey();

		AttributeClient attributeClient = new AttributeClientImpl(wsLocation,
				certificate, privateKey);
		return attributeClient;
	}

	public boolean isAdmin(String userId) {
		String username = getUsername(userId);
		LOG.debug("isAdmin: " + username);

		UserEntity user = this.entityManager.find(UserEntity.class, username);
		if (null == user) {
			return false;
		}

		return user.isAdmin();
	}

	public void bootstrap() {
		LOG.debug("bootstrapping...");
		UserEntity defaultAdminUser = this.entityManager.find(UserEntity.class,
				AuthorizationService.DEFAULT_ADMIN_USER);
		if (null == defaultAdminUser) {
			LOG.debug("adding default admin user: "
					+ AuthorizationService.DEFAULT_ADMIN_USER);
			defaultAdminUser = new UserEntity(
					AuthorizationService.DEFAULT_ADMIN_USER);
			this.entityManager.persist(defaultAdminUser);
		}
		if (false == defaultAdminUser.isAdmin()) {
			LOG.debug("resetting default admin user to admin privilege: "
					+ defaultAdminUser.getName());
			defaultAdminUser.setAdmin(true);
		}
	}
}
