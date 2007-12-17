/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.performance;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "PerformanceStartableBean")
public class PerformanceStartableBean extends AbstractInitBean {

	private static final String PERFORMANCE_ATTRIBUTE = "urn:net:lin-k:safe-online:attribute:test";
	public static final String PERFORMANCE_APPLICATION_NAME = "performance-application";

	public PerformanceStartableBean() {

		/*
		 * Create the performance user.
		 */
		this.authorizedUsers.put("performance", new AuthenticationDevice(
				"performance", null, null));

		/*
		 * Obtain the performance application identity.
		 */
		PrivateKeyEntry perfPrivateKeyEntry = PerformanceKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate perfCertificate = (X509Certificate) perfPrivateKeyEntry
				.getCertificate();

		/*
		 * Register the application and the application certificate.
		 */
		this.trustedCertificates.add(perfCertificate);
		this.registeredApplications.add(new Application(
				PERFORMANCE_APPLICATION_NAME, "owner", null, null, null, null,
				true, false, perfCertificate, true, IdScopeType.USER));

		/*
		 * Subscribe the performance user to the performance application.
		 */
		this.subscriptions.add(new Subscription(SubscriptionOwnerType.SUBJECT,
				"performance", PERFORMANCE_APPLICATION_NAME));

		/*
		 * Attribute Types.
		 */
		AttributeTypeEntity attributeType = new AttributeTypeEntity(
				PERFORMANCE_ATTRIBUTE, DatatypeType.STRING, true, true);
		this.attributeTypes.add(attributeType);

		/*
		 * Application Identities
		 */
		this.identities.add(new Identity(PERFORMANCE_APPLICATION_NAME,
				new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
						PERFORMANCE_ATTRIBUTE, true, false) }));
	}

	@Override
	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP - 1;
	}
}
