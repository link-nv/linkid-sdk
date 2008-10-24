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
import java.util.Properties;
import java.util.Set;

import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SmartCardConfigFactoryImpl implements SmartCardConfigFactory {

    public static final String SAFE_ONLINE_PKCS11_SC_CONFIG_RESOURCE_NAME = "META-INF/safe-online-pkcs11-sc-config.properties";

    private static final Log   LOG                                        = LogFactory.getLog(SmartCardConfigFactoryImpl.class);


    public List<SmartCardConfig> getSmartCardConfigs() {

        LOG.debug("get smart card configs");
        List<SmartCardConfig> smartCardConfigs = new LinkedList<SmartCardConfig>();
        Enumeration<URL> configResources = getConfigResources();
        if (null == configResources)
            return smartCardConfigs;
        while (configResources.hasMoreElements()) {
            URL configResource = configResources.nextElement();
            LOG.debug("config resource: " + configResource);

            Properties properties = new Properties();
            try {
                properties.load(configResource.openStream());
            } catch (IOException e) {
                LOG.error("IO error loading config file: " + e.getMessage(), e);
                continue;
            }

            String cardAlias = (String) properties.get("safe.online.pkcs11.sc.config.alias");
            SmartCardConfigImpl smartCardConfig = new SmartCardConfigImpl(cardAlias);

            String authenticationKeyAlias = (String) properties.get("safe.online.pkcs11.sc.config.keymapping.authentication");
            smartCardConfig.setAuthenticationKeyAlias(authenticationKeyAlias);

            String signatureKeyAlias = (String) properties.get("safe.online.pkcs11.sc.config.keymapping.signature");
            smartCardConfig.setSignatureKeyAlias(signatureKeyAlias);

            String identityExtractor = (String) properties.get("safe.online.pkcs11.sc.config.identity.extractor.classname");
            smartCardConfig.setIdentityExtractorClassname(identityExtractor);

            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                String keyStr = (String) key;
                if (false == keyStr.matches("safe\\.online\\.pkcs11\\.sc\\.config\\.library\\..*\\.match")) {
                    continue;
                }
                LOG.debug("keyStr: " + keyStr);
                String iterId = keyStr.substring("safe.online.pkcs11.sc.config.library.".length());
                iterId = iterId.substring(0, iterId.indexOf("."));
                LOG.debug("iterId: " + iterId);

                String match = properties.getProperty("safe.online.pkcs11.sc.config.library." + iterId + ".match");
                LOG.debug("match: " + match);
                List<String> driverLocations = new LinkedList<String>();
                for (Object locationKey : keys) {
                    String locationKeyStr = (String) locationKey;
                    if (false == locationKeyStr.matches("safe\\.online\\.pkcs11\\.sc\\.config\\.library\\." + iterId + "\\.location\\..*")) {
                        continue;
                    }
                    LOG.debug("locationkeyStr: " + locationKeyStr);
                    String location = properties.getProperty(locationKeyStr);
                    driverLocations.add(location);
                }
                smartCardConfig.addPkcs11DriverLocations(match, driverLocations);
            }

            smartCardConfigs.add(smartCardConfig);
        }
        return smartCardConfigs;
    }

    private Enumeration<URL> getConfigResources() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> configResources = classLoader.getResources(SAFE_ONLINE_PKCS11_SC_CONFIG_RESOURCE_NAME);
            return configResources;
        } catch (IOException e) {
            LOG.error("IO error: " + e.getMessage(), e);
            return null;
        }
    }
}
