/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell.osgi;

import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_API_ID;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_DEFAULT_API_ID;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_DEFAULT_PASSWORD;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_DEFAULT_URL;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_DEFAULT_USERNAME;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_GROUP_NAME;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_PASSWORD;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_URL;
import static net.link.safeonline.sms.clickatell.osgi.ClickatellSmsServiceActivator.CONFIG_USERNAME;

import java.net.MalformedURLException;
import java.net.URL;

import net.link.safeonline.osgi.OlasConfigurationService;
import net.link.safeonline.osgi.sms.SmsService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.sms.clickatell.ClickatellChannel;
import net.link.safeonline.sms.clickatell.exception.ClickatellException;
import net.link.safeonline.sms.clickatell.impl.ClickatellSoapChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <sub>Clickatell sms service.</sub></h2>
 * 
 * <p>
 * This class uses Clickatell to send an sms
 * </p>
 * 
 * <p>
 * <i>Feb 18, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class ClickatellSmsService implements SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(ClickatellSmsService.class);


    public ClickatellSmsService() {

    }

    /**
     * {@inheritDoc}
     */
    public void sendSms(String mobile, String message)
            throws SmsServiceException {

        OlasConfigurationService configuration = ClickatellSmsServiceActivator.configuration;

        String username = (String) configuration.getConfigurationValue(CONFIG_GROUP_NAME, CONFIG_USERNAME, CONFIG_DEFAULT_USERNAME);
        String password = (String) configuration.getConfigurationValue(CONFIG_GROUP_NAME, CONFIG_PASSWORD, CONFIG_DEFAULT_PASSWORD);
        Integer apiId = (Integer) configuration.getConfigurationValue(CONFIG_GROUP_NAME, CONFIG_API_ID, CONFIG_DEFAULT_API_ID);
        String urlString = (String) configuration.getConfigurationValue(CONFIG_GROUP_NAME, CONFIG_URL, CONFIG_DEFAULT_URL);

        LOG.debug("send sms to mobile " + mobile + " with message " + message);

        // Need to switch classloaders: the current thread classloaders is the
        // jboss one we need the osgi one to play nice with axis.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            ClickatellChannel channel = new ClickatellSoapChannel(new URL(urlString), apiId, username, password);
            channel.send(mobile, message);
        } catch (MalformedURLException e) {
            LOG.debug("Malformed URL", e);
            throw new SmsServiceException("Invalid URL", e);
        } catch (ClickatellException e) {
            LOG.debug("Got an exception from the Clickatell implementation", e);
            throw new SmsServiceException("Error while sending SMS via Clickatell", e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
