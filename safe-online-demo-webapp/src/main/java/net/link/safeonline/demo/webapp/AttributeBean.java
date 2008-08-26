/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.webapp;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import net.link.safeonline.demo.keystore.DemoKeyStoreUtil;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AttributeBean {

	private static final Log LOG = LogFactory.getLog(AttributeBean.class);

	private String attributeName;

	private String attributeWebServiceLocation;

	private String attributeValue;

	private String subjectLogin;

	private X509Certificate certificate;

	private PrivateKey privateKey;

	public String getSubjectLogin() {

		return this.subjectLogin;
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

	public X509Certificate getCertificate() {

		return this.certificate;
	}

	public PrivateKey getPrivateKey() {

		return this.privateKey;
	}

	private void loadCertificate() {

		PrivateKeyEntry privateKeyEntry = DemoKeyStoreUtil.getPrivateKeyEntry();
		this.certificate = (X509Certificate) privateKeyEntry.getCertificate();
		this.privateKey = privateKeyEntry.getPrivateKey();
	}

	public String getAttributeValue() {

		if (null == this.attributeValue) {
			loadCertificate();
			AttributeClient attributeClient = new AttributeClientImpl(
					this.attributeWebServiceLocation, this.certificate,
					this.privateKey);
			try {
				this.attributeValue = attributeClient.getAttributeValue(
						this.subjectLogin, this.attributeName, String.class);
			} catch (AttributeNotFoundException e) {
				LOG.error("attribute not found: " + e.getMessage());
				return "[attribute not found]";
			} catch (RequestDeniedException e) {
				LOG.error("request denied");
				return "[request denied]";
			} catch (WSClientTransportException e) {
				LOG.error("connection error. check your SSL setup");
				return "[connection error. check your SSL setup]";
			} catch (AttributeUnavailableException e) {
				LOG.error("attribute unavailable: " + this.attributeName);
				return "[attribute unavailable: " + this.attributeName + "]";
			}
		}
		return this.attributeValue;
	}
}
