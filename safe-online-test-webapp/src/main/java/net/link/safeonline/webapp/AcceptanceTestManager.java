/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.server.SeleniumServer;


/**
 * Acceptance test manager based on the Selenium testing framework.
 * 
 * This component will launch the selenium server on port 4455.
 * 
 * Make sure that the SafeOnline web applications are up and running.
 * 
 * Note: JBoss AS already runs on the default selenium server port 4444.
 * 
 * @author fcorneli
 * 
 */
public class AcceptanceTestManager {

    private static final Log LOG                  = LogFactory.getLog(AcceptanceTestManager.class);

    private Selenium         selenium;

    SeleniumServer           seleniumServer;

    public static final int  SELENIUM_SERVER_PORT = 4455;

    String                   safeOnlineLocation;


    public void setUp()
            throws Exception {

        seleniumServer = new SeleniumServer(SELENIUM_SERVER_PORT);
        seleniumServer.start();
        Properties properties = new Properties();
        InputStream propConfigInputStream = AcceptanceTestManager.class.getResourceAsStream("/test-accept-config.properties");
        properties.load(propConfigInputStream);
        safeOnlineLocation = "http://" + properties.getProperty("safeonline.location");
        LOG.debug("SafeOnline location: " + safeOnlineLocation);
        selenium = new DefaultSelenium("localhost", SELENIUM_SERVER_PORT, "*firefox", safeOnlineLocation);
        selenium.start();

        Page.setSelenium(selenium);
        Page.setAcceptanceManager(this);
    }

    public void tearDown()
            throws Exception {

        selenium.stop();
        seleniumServer.stop();
    }

    public void setContext(String context) {

        selenium.setContext(context);
        selenium.setBrowserLogLevel(SeleniumLogLevels.INFO);
    }

    public Selenium getSelenium() {

        if (null == selenium)
            throw new IllegalStateException("call setUp first");
        return selenium;
    }

    public String getSafeOnlineLocation() {

        return safeOnlineLocation;
    }

    public String getLocation() {

        return selenium.getLocation();
    }
}
