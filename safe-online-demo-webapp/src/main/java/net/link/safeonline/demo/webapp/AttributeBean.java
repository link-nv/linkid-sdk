/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.webapp;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.sdk.attrib.AttributeClient;
import net.link.safeonline.sdk.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.attrib.AttributeNotFoundException;
import net.link.safeonline.sdk.attrib.KeyStoreUtils;

public class AttributeBean {

	private static final Log LOG = LogFactory.getLog(AttributeBean.class);

	public static final String KEYSTORE_RESOURCE = "safe-online-demo-keystore.jks";

	private String attributeName;

	private String attributeWebServiceLocation;

	private String attributeValue;

	private String subjectLogin;

	private X509Certificate certificate;

	private PrivateKey privateKey;

	public String getSubjectLogin() {
		return subjectLogin;
	}

	public void setSubjectLogin(String subjectLogin) {
		this.subjectLogin = subjectLogin;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeWebServiceLocation() {
		return this.attributeWebServiceLocation;
	}

	public void setAttributeWebServiceLocation(
			String attributeWebServiceLocation) {
		this.attributeWebServiceLocation = attributeWebServiceLocation;
	}

	public void loadCertificate() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream keyStoreInputStream = classLoader
				.getResourceAsStream(KEYSTORE_RESOURCE);
		if (null == keyStoreInputStream) {
			throw new RuntimeException("keystore not found: "
					+ KEYSTORE_RESOURCE);
		}
		PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(
				"jks", keyStoreInputStream, "secret", "secret");
		this.privateKey = privateKeyEntry.getPrivateKey();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
	}

	public X509Certificate getCertificate() {
		return this.certificate;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public String getAttributeValue() {
		if (null == this.attributeValue) {
			loadCertificate();
			AttributeClient attributeClient = new AttributeClientImpl(
					this.attributeWebServiceLocation, this.certificate,
					this.privateKey);
			try {
				this.attributeValue = attributeClient.getAttributeValue(
						this.subjectLogin, this.attributeName);
			} catch (AttributeNotFoundException e) {
				LOG.error("attribute not found: " + e.getMessage());
				return "[attribute not found]";
			}
		}
		return this.attributeValue;
	}
}
