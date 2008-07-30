/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.entity.DriverExceptionEntity;
import net.link.safeonline.performance.entity.DriverProfileEntity;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ProfileDataEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.Scenario;
import net.link.safeonline.performance.scenario.ScenarioController;
import net.link.safeonline.performance.scenario.charts.Chart;
import net.link.safeonline.performance.scenario.script.RegisteredScripts;
import net.link.safeonline.performance.service.DriverExceptionService;
import net.link.safeonline.performance.service.ExecutionService;
import net.link.safeonline.performance.service.ProfileDataService;
import net.link.safeonline.performance.service.ScenarioTimingService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.TransactionTimeout;

/**
 * <h2>{@link ScenarioControllerBean}<br>
 * <sub>This bean is the heart of the scenario application.</sub></h2>
 * 
 * <p>
 * We take care of preparing scenario execution and launching a single scenario
 * run. As these methods are called, entity objects are updated with state that
 * can later be used to graph out the progress of the scenario execution.<br>
 * <br>
 * Charts are also generated in this bean as registered by the scenario.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@Local(ScenarioController.class)
@LocalBinding(jndiBinding = ScenarioController.BINDING)
public class ScenarioControllerBean implements ScenarioController {

    /*
     * Timeout values for long running methods (in seconds).
     */
    private static final int             CHARTING_TIMEOUT           = 10 * 60 * 60;
    private static final int             SCENARIO_EXECUTION_TIMEOUT = 10 * 60;

    private static final Log             LOG                        = LogFactory
                                                                            .getLog(ScenarioControllerBean.class);
    private static final int             DATA_POINTS                = 800;

    private static MBeanServerConnection rmi;

    static {
        try {
            rmi = (MBeanServerConnection) getInitialContext().lookup(
                    "jmx/invoker/RMIAdaptor");
        } catch (NamingException e) {
            LOG.error("JMX unavailable.", e);
        }
    }

    @EJB
    private ExecutionService             executionService;

    @EJB
    private ProfileDataService           profileDataService;

    @EJB
    private DriverExceptionService       driverExceptionService;

    @EJB
    private ScenarioTimingService        scenarioTimingService;

    @Resource
    SessionContext                       ctx;


    /**
     * {@inheritDoc}
     */
    @TransactionTimeout(SCENARIO_EXECUTION_TIMEOUT)
    public void execute(Date startTime) throws Exception {

        ExecutionEntity execution = this.executionService
                .getExecution(startTime);
        ScenarioTimingEntity agentTime = this.executionService.start(execution);
        agentTime.setStartMemory(getFreeMemory());

        Scenario scenario = createScenario(execution.getScenarioName());
        scenario.prepare(execution, agentTime);

        try {
            scenario.run();
        }

        // Must catch to prevent rollback.
        catch (Throwable t) {
            LOG.error("Scenario execution failed:", t);
        }

        finally {
            agentTime.setEndMemory(getFreeMemory());
            execution.dirtySpeed();

            agentTime.stop();
        }
    }

    /**
     * Create an instance of the given scenario.
     */
    private Scenario createScenario(String scenario) {

        try {
            return loadClass(Scenario.class, scenario).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Configured scenario '" + scenario
                    + "' cannot be created.", e);
        }
    }

    /**
     * Load a class with the given class name. TODO: Describe method.
     */
    @SuppressWarnings("unchecked")
    private <C> Class<C> loadClass(@SuppressWarnings("unused") Class<C> clazz,
            String className) throws ClassNotFoundException {

        return (Class<C>) Thread.currentThread().getContextClassLoader()
                .loadClass(className);
    }

    /**
     * {@inheritDoc}
     */
    public Date prepare(ExecutionMetadata metaData) {

        // Create the execution and fill it up with metadata.
        ExecutionEntity execution = this.executionService.addExecution(metaData
                .getScenarioName(), metaData.getAgents(),
                metaData.getWorkers(), metaData.getStartTime(), metaData
                        .getDuration(), metaData.getHostname(), metaData
                        .isSsl());
        createScenario(execution.getScenarioName()).prepare(execution, null);

        return execution.getStartTime();
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getScenarios() {

        Set<String> scenarios = new HashSet<String>();
        for (Class<? extends Scenario> scenario : RegisteredScripts
                .getRegisteredScenarios()) {
            scenarios.add(scenario.getName());
        }

        return scenarios;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Date> getExecutions() {

        return this.executionService.getExecutions();
    }

    /**
     * {@inheritDoc}
     */
    public ExecutionMetadata getExecutionMetadata(Date executionId) {

        ExecutionEntity execution = this.executionService
                .getExecution(executionId);

        return ExecutionMetadata.createResponse(execution.getScenarioName(),
                getDescription(executionId), execution.getAgents(), execution
                        .getWorkers(), execution.getStartTime(), execution
                        .getDuration(), execution.getHostname(), execution
                        .isSsl(), execution.getSpeed());
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription(String scenario) {

        return createScenario(scenario).getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription(Date executionId) {

        ExecutionEntity execution = this.executionService
                .getExecution(executionId);
        StringBuffer description = new StringBuffer();

        for (DriverProfileEntity profile : new TreeSet<DriverProfileEntity>(
                execution.getProfiles())) {
            try {
                String driverDescription = (String) loadClass(null,
                        profile.getDriverClassName()).getField("DESCRIPTION")
                        .get(null);

                description.append("<li>").append(driverDescription).append(
                        "</li>\n");
            } catch (Exception e) {
            }
        }

        if (description.length() > 0) {
            description.insert(0, "\n\nThe following drivers were used:<ul>\n");
            description.append("</ul>");
        }

        return getDescription(execution.getScenarioName())
                + description.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Double getProgress(Date executionStartTime) {

        ExecutionEntity execution = this.executionService
                .getExecution(executionStartTime);

        return execution.getChartingProgress();
    }

    /**
     * {@inheritDoc}
     */
    @TransactionTimeout(CHARTING_TIMEOUT)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Map<String, byte[][]> createCharts(Date executionStartTime) {

        boolean ready = false;
        ExecutionEntity execution = this.executionService
                .getExecution(executionStartTime);

        try {
            // Start the progress.
            execution.setChartingProgress(0d);

            // Check whether charts were already generated for this execution.
            Map<String, byte[][]> images = execution.getCharts();
            if (images != null) {
                LOG.debug("Charts already done.");
                ready = true;
                return images;
            }

            List<? extends Chart> charts = createScenario(
                    execution.getScenarioName()).getCharts();
            LOG.debug("Creating " + charts.size() + "charts..");

            // Divide the charts over three lists depending on data they chart.
            List<Chart> dataCharts, errorCharts, timingCharts;
            dataCharts = new ArrayList<Chart>();
            errorCharts = new ArrayList<Chart>();
            timingCharts = new ArrayList<Chart>();
            for (Chart chart : charts) {
                if (chart.isDataProcessed()) {
                    dataCharts.add(chart);
                }
                if (chart.isErrorProcessed()) {
                    errorCharts.add(chart);
                }
                if (chart.isTimingProcessed()) {
                    timingCharts.add(chart);
                }
            }
            LOG.debug(" - " + timingCharts.size() + " need(s) timings.");
            LOG.debug(" - " + dataCharts.size() + " need(s) data.");
            LOG.debug(" - " + errorCharts.size() + " need(s) errors.");

            // Chart scenario timing data.
            LOG.debug(" - Starting timings..");
            if (!timingCharts.isEmpty()) {
                List<ScenarioTimingEntity> scenarioTimings = this.scenarioTimingService
                        .getExecutionTimings(execution);
                double total = scenarioTimings.size(), current = 0;
                LOG.debug(" - - Total: " + total);
                for (ScenarioTimingEntity timing : scenarioTimings) {
                    if (timing != null) {
                        for (Chart chart : timingCharts) {
                            try {
                                chart.processTiming(timing);
                            } catch (Exception e) {
                                LOG.error("Charting Timing Failed:", e);
                            }
                        }
                    }

                    execution.setChartingProgress(++current / total * 0.4);
                    LOG.debug(String.format(" - - %01.2f%% (timings)", current
                            / total * 0.4 * 100));
                }
            }

            // Chart driver data.
            Set<DriverProfileEntity> profiles = execution.getProfiles();
            double totalProfiles = profiles.size(), currentProfile = 0;
            for (DriverProfileEntity profile : profiles) {

                // Chart data.
                LOG.debug(" - Starting " + profile.getDriverClassName() + "..");
                if (!dataCharts.isEmpty()) {
                    List<ProfileDataEntity> profileData = this.profileDataService
                            .getProfileData(profile, DATA_POINTS);
                    double total = profileData.size(), current = 0;
                    LOG.debug(" - - Total: " + total);
                    for (ProfileDataEntity data : profileData) {
                        if (data != null) {
                            LOG.debug("Processing: " + data);

                            for (Chart chart : dataCharts) {
                                try {
                                    chart.processData(data);
                                } catch (Exception e) {
                                    LOG.error("Charting Data Failed:", e);
                                }
                            }
                        }

                        execution
                                .setChartingProgress((total * currentProfile + ++current)
                                        / (total * totalProfiles) * 0.4 + 0.4);
                        LOG.debug(String.format(" - - %01.2f%% (data)", ((total
                                * currentProfile + current)
                                / (total * totalProfiles) * 0.4 + 0.4) * 100));
                    }
                }

                // Chart errors.
                LOG.debug(" - - Errors..");
                if (!errorCharts.isEmpty()) {
                    List<DriverExceptionEntity> profileErrors = this.driverExceptionService
                            .getProfileErrors(profile, DATA_POINTS);
                    for (DriverExceptionEntity error : profileErrors)
                        if (error != null) {
                            for (Chart chart : errorCharts) {
                                try {
                                    chart.processError(error);
                                } catch (Exception e) {
                                    LOG.error("Charting Error Failed:", e);
                                }
                            }
                        }
                }

                ++currentProfile;
            }

            LOG.debug(" - Starting post-processing..");
            double total = charts.size(), current = 0;
            LOG.debug(" - - Total: " + total);
            for (Chart chart : charts) {
                chart.postProcess();

                execution.setChartingProgress(++current / total * 0.1 + 0.8);
                LOG.debug(String.format(" - - %01.2f%% (post)", (current
                        / total * 0.1 + 0.8) * 100));
            }

            LOG.debug(" - Starting rendering..");
            images = new LinkedHashMap<String, byte[][]>();
            LOG.debug(" - - Total: " + total);
            current = 0;
            for (Chart chart : charts) {
                byte[][] image = chart.render(DATA_POINTS);
                if (image != null) {
                    images.put(chart.getTitle(), image);
                }

                execution.setChartingProgress(++current / total * 0.1 + 0.9);
                LOG.debug(String.format(" - - %01.2f%% (render)", (current
                        / total * 0.1 + 0.9) * 100));
            }

            execution.setCharts(images);
            ready = true;

            return images;
        }

        finally {
            LOG.debug(" - Stopped Charting; Ready? " + ready);
            execution.setChartingProgress(ready ? null : 1d);
        }
    }

    private long getFreeMemory() {

        try {
            return (Long) rmi.getAttribute(new ObjectName(
                    "jboss.system:type=ServerInfo"), "FreeMemory");
        } catch (Exception e) {
            LOG.error("Failed to read in free memory through JMX.", e);
        }

        return -1;
    }

    private static InitialContext getInitialContext() throws NamingException {

        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.jnp.interfaces.NamingContextFactory");
        environment.put(Context.PROVIDER_URL, "localhost:1099");

        return new InitialContext(environment);
    }
}
