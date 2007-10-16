/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.util.webapp.filter.ProfileStats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author mbillemo
 */
public class IdMappingDriver extends ProfileDriver {

	private static final Log LOG = LogFactory.getLog(IdMappingDriver.class);

	private String host;

	private String user;

	private NameIdentifierMappingClientImpl service;

	/**
	 * Create a new {@link IdMappingDriver} instance.
	 * 
	 * @param hostname
	 *            The hostname of the host that's running the idmapping
	 *            webservice.
	 * @param username
	 *            The username to map to ID in this test.
	 */
	public IdMappingDriver(String hostname, String username) {

		this.host = hostname;
		this.user = username;
	}

	@Override
	protected void prepare() throws Exception {

		PrivateKeyEntry serviceEntry = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();

		if (!(serviceEntry.getCertificate() instanceof X509Certificate))
			throw new RuntimeException(
					"The certificate in the keystore needs to be of X509 format.");

		this.service = new NameIdentifierMappingClientImpl(this.host,
				(X509Certificate) serviceEntry.getCertificate(), serviceEntry
						.getPrivateKey());
	}

	@Override
	protected Map<ProfileStats, Number> run() throws Exception {

		Map<ProfileStats, Number> stats = new HashMap<ProfileStats, Number>();

		LOG.debug("retrieving user ID for " + this.user);
		this.service.getUserId(this.user);

		for (ProfileStats stat : ProfileStats.values())
			stats.put(stat, getHeader(stat));

		return stats;
	}

	private Number getHeader(ProfileStats header) {

		Object result = this.service.getHeader(header.getHeader()).getFirst();
		try {
			return Integer.parseInt(String.valueOf(result));
		} catch (NumberFormatException a) {
			try {
				return Double.parseDouble(String.valueOf(result));
			} catch (NumberFormatException b) {
				try {
					return Float.parseFloat(String.valueOf(result));
				} catch (NumberFormatException c) {
					try {
						return Long.parseLong(String.valueOf(result));
					} catch (NumberFormatException d) {
						try {
							return Short.parseShort(String.valueOf(result));
						} catch (NumberFormatException e) {
							throw new NumberFormatException(
									"The header data for "
											+ header.getHeader()
											+ " does not contain a valid number: "
											+ result);
						}
					}
				}
			}
		}
	}
}
