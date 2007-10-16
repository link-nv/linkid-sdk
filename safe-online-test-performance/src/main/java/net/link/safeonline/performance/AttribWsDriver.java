/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.net.ConnectException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.demo.mandate.keystore.DemoMandateKeyStoreUtils;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.prescription.keystore.DemoPrescriptionKeyStoreUtils;
import net.link.safeonline.demo.ticket.keystore.DemoTicketKeyStoreUtils;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.util.webapp.filter.ProfileStats;

/**
 * TODO: Work.
 * 
 * @author mbillemo
 */
public class AttribWsDriver extends ProfileDriver {

	private static final Log LOG = LogFactory.getLog(AttribWsDriver.class);

	private static final String OLAS_HOSTNAME = "localhost";

	private HashMap<String, AttributeClient> services;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prepare() {

		this.services = new HashMap<String, AttributeClient>();

		addService(this.services, "Lawyer Demo", DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry());
		addService(this.services, "Mandate Demo", DemoMandateKeyStoreUtils
				.getPrivateKeyEntry());
		addService(this.services, "Payment Demo", DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry());
		addService(this.services, "Prescription Demo",
				DemoPrescriptionKeyStoreUtils.getPrivateKeyEntry());
		addService(this.services, "Ticket Demo", DemoTicketKeyStoreUtils
				.getPrivateKeyEntry());
	}

	private void addService(Map<String, AttributeClient> demoServices,
			String serviceName, PrivateKeyEntry serviceEntry) {

		LOG.debug("preparing service " + serviceName);

		if (!(serviceEntry.getCertificate() instanceof X509Certificate)) {
			LOG.error("invalid key format: type is not X509 but "
					+ serviceEntry.getCertificate().getType());
			throw new RuntimeException(
					"The certificate in the keystore needs to be of X509 format.");
		}

		demoServices.put("lawyer", new AttributeClientImpl(OLAS_HOSTNAME,
				(X509Certificate) serviceEntry.getCertificate(), serviceEntry
						.getPrivateKey()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ProfileStats, Number> run() {

		LOG.debug("profiling " + this.services.size() + " services");

		for (Map.Entry<String, AttributeClient> serviceEntry : this.services
				.entrySet()) {
			AttributeClient service = serviceEntry.getValue();
			String serviceName = serviceEntry.getKey();

			LOG.debug("profiling " + serviceName);
			try {
				service.getAttributeValues("TODO: userId");
			} catch (ConnectException e) {
				LOG.error("couldn't connect to service provider", e);
			} catch (RequestDeniedException e) {
				LOG.error("service provider denied request", e);
			} catch (AttributeNotFoundException e) {
				LOG.error("attributes couldn't be found", e);
			}
		}

		return null;
	}
}
