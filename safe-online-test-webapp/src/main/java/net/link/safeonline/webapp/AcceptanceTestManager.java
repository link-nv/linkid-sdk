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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;


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


    public void setUp() throws Exception {

        this.seleniumServer = new SeleniumServer(SELENIUM_SERVER_PORT);
        this.seleniumServer.start();
        Properties properties = new Properties();
        InputStream propConfigInputStream = AcceptanceTestManager.class
                .getResourceAsStream("/test-accept-config.properties");
        properties.load(propConfigInputStream);
        this.safeOnlineLocation = "http://" + properties.getProperty("safeonline.location");
        LOG.debug("SafeOnline location: " + this.safeOnlineLocation);
        this.selenium = new DefaultSelenium("localhost", SELENIUM_SERVER_PORT, "*firefox", this.safeOnlineLocation);
        this.selenium.start();

        Page.setSelenium(this.selenium);
        Page.setAcceptanceManager(this);
    }

    public void tearDown() throws Exception {

        this.selenium.stop();
        this.seleniumServer.stop();
    }

    public void setContext(String context) {

        this.selenium.setContext(context);
        this.selenium.setBrowserLogLevel(SeleniumLogLevels.INFO);
    }

    public Selenium getSelenium() {

        if (null == this.selenium) {
            throw new IllegalStateException("call setUp first");
        }
        return this.selenium;
    }

    public String getSafeOnlineLocation() {

        return this.safeOnlineLocation;
    }

    public String getLocation() {

        return this.selenium.getLocation();
    }
}
