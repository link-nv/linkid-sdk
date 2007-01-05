/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.lin_k.safe_online.pkcs11_sc_config._1.AtrType;
import net.lin_k.safe_online.pkcs11_sc_config._1.AtrsType;
import net.lin_k.safe_online.pkcs11_sc_config._1.IdentityExtractorType;
import net.lin_k.safe_online.pkcs11_sc_config._1.KeyType;
import net.lin_k.safe_online.pkcs11_sc_config._1.KeyTypeType;
import net.lin_k.safe_online.pkcs11_sc_config._1.ObjectFactory;
import net.lin_k.safe_online.pkcs11_sc_config._1.Pkcs11KeystoreMappingType;
import net.lin_k.safe_online.pkcs11_sc_config._1.Pkcs11LibraryType;
import net.lin_k.safe_online.pkcs11_sc_config._1.Pkcs11ScConfigType;
import net.lin_k.safe_online.pkcs11_sc_config._1.PlatformType;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XmlSmartCardConfigFactory implements SmartCardConfigFactory {

	public static final String SAFE_ONLINE_PKCS11_SC_CONFIG_RESOURCE_NAME = "META-INF/safe-online-pkcs11-sc-config.xml";

	private static final Log LOG = LogFactory
			.getLog(XmlSmartCardConfigFactory.class);

	public List<SmartCardConfig> getSmartCardConfigs() {
		LOG.debug("get smart card configs");
		List<SmartCardConfig> smartCardConfigs = new LinkedList<SmartCardConfig>();
		Enumeration<URL> configResources = getConfigResources();
		if (null == configResources) {
			return smartCardConfigs;
		}
		while (configResources.hasMoreElements()) {
			URL configResource = configResources.nextElement();
			LOG.debug("config resource: " + configResource);
			Pkcs11ScConfigType pkcs11ScConfig = getPkcs11ScConfig(configResource);
			if (null == pkcs11ScConfig) {
				continue;
			}
			String alias = pkcs11ScConfig.getAlias();
			SmartCardConfigImpl smartCardConfig = new SmartCardConfigImpl(alias);

			AtrsType atrs = pkcs11ScConfig.getAtrs();
			List<AtrType> atrList = atrs.getAtr();
			for (AtrType atr : atrList) {
				List<String> bytesStr = atr.getByte();
				byte[] bytes = new byte[bytesStr.size()];
				int idx = 0;
				for (String byteStr : bytesStr) {
					if (byteStr.toLowerCase().startsWith("0x")) {
						byteStr = byteStr.substring(2);
						bytes[idx++] = (byte) (Integer.parseInt(byteStr, 16) & 0xff);
					} else {
						bytes[idx++] = (byte) (Integer.parseInt(byteStr) & 0xff);
					}
				}
				smartCardConfig.addSupportedATR(bytes);
			}

			Pkcs11KeystoreMappingType pkcs11KeystoreMapping = pkcs11ScConfig
					.getPkcs11KeystoreMapping();
			List<KeyType> keys = pkcs11KeystoreMapping.getKey();
			for (KeyType key : keys) {
				KeyTypeType keyType = key.getType();
				switch (keyType) {
				case AUTHENTICATION:
					smartCardConfig.setAuthenticationKeyAlias(key.getAlias());
					break;
				case SIGNATURE:
					smartCardConfig.setSignatureKeyAlias(key.getAlias());
					break;
				}
				;
			}

			Pkcs11LibraryType pkcs11Library = pkcs11ScConfig.getPkcs11Library();
			List<PlatformType> platforms = pkcs11Library.getPlatform();
			for (PlatformType platform : platforms) {
				String platformMatch = platform.getMatch();
				List<String> driverLocations = platform.getLocation();
				smartCardConfig.addPkcs11DriverLocations(platformMatch,
						driverLocations);
			}

			IdentityExtractorType identityExtractor = pkcs11ScConfig
					.getIdentityExtractor();
			if (null != identityExtractor) {
				String identityExtractorClassname = identityExtractor
						.getClassname();
				smartCardConfig
						.setIdentityExtractorClassname(identityExtractorClassname);
			}

			smartCardConfigs.add(smartCardConfig);
		}
		return smartCardConfigs;
	}

	private Enumeration<URL> getConfigResources() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Enumeration<URL> configResources = classLoader
					.getResources(SAFE_ONLINE_PKCS11_SC_CONFIG_RESOURCE_NAME);
			return configResources;
		} catch (IOException e) {
			LOG.error("IO error: " + e.getMessage(), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Pkcs11ScConfigType getPkcs11ScConfig(URL configResource) {
		try {
			JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			JAXBElement<Pkcs11ScConfigType> pkcs11ScConfigElement = (JAXBElement<Pkcs11ScConfigType>) unmarshaller
					.unmarshal(configResource);
			Pkcs11ScConfigType pkcs11ScConfig = pkcs11ScConfigElement
					.getValue();
			return pkcs11ScConfig;
		} catch (JAXBException e) {
			LOG.error("JAXB error: " + e.getMessage(), e);
			return null;
		}
	}
}
