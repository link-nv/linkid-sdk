/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.script;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.model.performance.PerformanceService;
import net.link.safeonline.performance.drivers.AttribDriver;
import net.link.safeonline.performance.drivers.AuthDriver;
import net.link.safeonline.performance.drivers.IdMappingDriver;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.charts.AbstractChart;
import net.link.safeonline.performance.scenario.charts.Chart;
import net.link.safeonline.performance.scenario.charts.ScenarioDriverDurationsChart;
import net.link.safeonline.performance.scenario.charts.ScenarioDurationsChart;
import net.link.safeonline.performance.scenario.charts.ScenarioExceptionsChart;
import net.link.safeonline.performance.scenario.charts.ScenarioMemoryChart;
import net.link.safeonline.performance.scenario.charts.ScenarioQueueChart;
import net.link.safeonline.performance.scenario.charts.ScenarioSpeedChart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link BasicScenario}<br>
 * <sub>A basic scenario that runs all basic drivers to test OLAS on every field available through the SDK.</sub></h2>
 * 
 * <p>
 * We perform the following, in order:
 * <ul>
 * <li>{@link AuthDriver#login(PrivateKeyEntry, String, String, String)}</li>
 * <li>{@link IdMappingDriver#getUserId(PrivateKeyEntry, String)}</li>
 * <li>{@link AttribDriver#getAttributes(PrivateKeyEntry, String)}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class BasicScenario implements Scenario {

    private static final Log    LOG             = LogFactory.getLog(BasicScenario.class);

    private static final String applicationName = "performance-application";
    private static final String username        = "performance";
    private static final String password        = "performance";

    private PrivateKeyEntry     applicationKey;
    private AttribDriver        attribDriver;
    private IdMappingDriver     idDriver;
    private AuthDriver          authDriver;


    /**
     * {@inheritDoc}
     */
    public String getDescription() {

        return "This scenario implements all standard drivers.\n"
                + "It is basically a scenario used to test each part of OLAS available through the SDK.";
    }

    public void prepare(ExecutionEntity execution, ScenarioTimingEntity agentTime) {

        LOG.debug("retrieving performance keys..");
        try {
            PerformanceService service = (PerformanceService) getInitialContext(execution.getHostname()).lookup(PerformanceService.BINDING);
            this.applicationKey = new KeyStore.PrivateKeyEntry(service.getPrivateKey(), new Certificate[] { service.getCertificate() });
        } catch (NamingException e) {
            LOG.error("OLAS couldn't provide performance keys.", e);
        }
        if (this.applicationKey == null) {
            this.applicationKey = PerformanceKeyStoreUtils.getPrivateKeyEntry();
        }

        LOG.debug("building drivers..");
        this.authDriver = new AuthDriver(execution, agentTime);
        this.attribDriver = new AttribDriver(execution, agentTime);
        this.idDriver = new IdMappingDriver(execution, agentTime);
    }

    /**
     * @{inheritDoc
     */
    public void run() {

        if (this.applicationKey == null) {
            throw new IllegalStateException("Performance keys not set up. Perhaps you didn't call prepare?");
        }

        /* Logging in. */
        String loginUserId = this.authDriver.login(this.applicationKey, applicationName, username, password);
        if (loginUserId == null) {
            throw new IllegalStateException("Login failed.");
        }

        /* Verifying UUID. */
        String mappedUserId = this.idDriver.getUserId(this.applicationKey, username);
        if (!loginUserId.equals(mappedUserId)) {
            throw new IllegalStateException("Login ID doesn't match mapped ID.");
        }

        /* Reading attributes. */
        this.attribDriver.getAttributes(this.applicationKey, mappedUserId);
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends Chart> getCharts() {

        List<Chart> charts = new ArrayList<Chart>();
        charts.add(new ScenarioDurationsChart());
        charts.add(new ScenarioMemoryChart());
        charts.add(new ScenarioQueueChart());
        charts.add(new ScenarioSpeedChart(5 * 60 * 1000));
        charts.add(new ScenarioDriverDurationsChart());
        charts.add(new ScenarioExceptionsChart());

        AbstractChart.sharedTimeAxis(charts);

        return charts;
    }

    /**
     * Retrieve an {@link InitialContext} for the JNDI of the AS on the given host.
     */
    private static InitialContext getInitialContext(String hostname) throws NamingException {

        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "jnp://" + hostname + ":1099");

        return new InitialContext(environment);
    }
}
