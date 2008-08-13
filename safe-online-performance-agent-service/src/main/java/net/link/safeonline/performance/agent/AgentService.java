/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.performance.agent;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.scenario.ExecutionMetadata;
import net.link.safeonline.performance.scenario.ScenarioController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AgentService}<br>
 * <sub>This class provides all functionality of the agent that is available to the console.</sub></h2>
 * 
 * <p>
 * This MBean launches the broadcaster service that provides agent visibility in JGroups and the deployer service that
 * is used for deploying uploaded scenarios. It also keeps the current state of the agent (see {@link AgentState}).<br>
 * <br>
 * The agent service delegates requests for uploading, deploying, executing and charting scenarios.<br>
 * <br>
 * Metadata on previously performed executions are also cached by this agent as they are requested by the console.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class AgentService implements AgentServiceMBean {

    static final Log                         LOG = LogFactory.getLog(AgentService.class);

    private Map<Date, Map<String, byte[][]>> charts;
    private AgentBroadcaster                 broadcaster;
    private ScenarioDeployer                 deployer;
    private ScenarioExecutor                 executor;
    private Throwable                        error;

    private boolean                          charting;


    public AgentService() {

        this.charts = new HashMap<Date, Map<String, byte[][]>>();
        this.deployer = new ScenarioDeployer();
        this.broadcaster = new AgentBroadcaster();
    }

    /**
     * {@inheritDoc}
     */
    public void start() {

        this.broadcaster.start();
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {

        this.broadcaster.stop();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStarted() {

        return this.broadcaster.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    public String getGroup() {

        return this.broadcaster.getGroup();
    }

    /**
     * {@inheritDoc}
     */
    public void setGroup(String group) {

        this.broadcaster.setGroup(group);
    }

    /**
     * {@inheritDoc}
     */
    public AgentState getState() {

        try {
            if (getScenarioController() == null)
                throw new NamingException("ScenarioController not available.");

            Set<Date> executions = getScenarioController().getExecutions();
            if (executions == null || executions.isEmpty())
                return AgentState.DEPLOY;

            else if (this.charts.isEmpty())
                return AgentState.EXECUTE;

            else
                return AgentState.CHART;
        }

        catch (NamingException e) {
            return this.deployer.isUploaded()? AgentState.UPLOAD: AgentState.RESET;
        }
    }

    /**
     * {@inheritDoc}
     */
    public AgentState getTransit() {

        if (this.deployer.isUploading())
            return AgentState.UPLOAD;

        if (this.deployer.isDeploying())
            return AgentState.DEPLOY;

        if (this.executor != null && this.executor.isAlive())
            return AgentState.EXECUTE;

        if (this.charting)
            return AgentState.CHART;

        return AgentState.RESET;
    }

    /**
     * {@inheritDoc}
     */
    public void resetTransit() {

        if (this.executor != null) {
            this.executor.halt();
            this.executor = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public ScenarioExecution getCharts(Date startTime) {

        try {
            return getExecution(startTime, true);
        }

        catch (Throwable e) {
            setError(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Throwable getError() {

        return this.error;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getScenarios() {

        try {
            return getScenarioController().getScenarios();
        } catch (NamingException e) {
            return new HashSet<String>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<ScenarioExecution> getExecutions() throws NamingException {

        Set<ScenarioExecution> executions = new HashSet<ScenarioExecution>();
        for (Date startTime : getScenarioController().getExecutions())
            executions.add(getExecution(startTime, false));

        return executions;
    }

    /**
     * @param error
     *            An error that occurred while interacting with this client.
     */
    public void setError(Throwable error) {

        this.error = error;

        if (error != null)
            LOG.error("The following occurred during " + getTransit(), error);
    }

    /**
     * {@inheritDoc}
     */
    public void upload(byte[] application) {

        try {
            this.deployer.upload(application);
        }

        catch (Throwable e) {
            setError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deploy() {

        try {
            this.deployer.deploy();
            this.charts.clear();
        }

        catch (Throwable e) {
            setError(e);
        }
    }

    public void execute(String scenarioName, Integer agents, Integer workers, Long duration, String hostname,
            Boolean useSsl, Date startTime) {

        try {
            ExecutionMetadata request = ExecutionMetadata.createRequest(scenarioName, agents, workers, startTime,
                    duration, hostname, useSsl);

            (this.executor = new ScenarioExecutor(request, this)).start();
        }

        catch (Throwable e) {
            setError(e);
        }
    }

    /**
     * Returns execution metadata for the execution started at the given time. Previously generated charts for this
     * execution are cached in a map.
     */
    private ScenarioExecution getExecution(Date startTime, boolean useCharts) {

        if (startTime == null)
            return null;

        try {
            this.charting = useCharts;
            ExecutionMetadata metaData = getScenarioController().getExecutionMetadata(startTime);

            ScenarioExecution execution = new ScenarioExecution(metaData.getScenarioName(), metaData
                    .getScenarioDescription(), metaData.getAgents(), metaData.getWorkers(), metaData.getStartTime(),
                    metaData.getDuration(), metaData.getHostname(), metaData.isSsl(), metaData.getSpeed());

            if (useCharts) {
                LOG.debug("Looking for charts on id: " + startTime);
                LOG.debug(" - Chart keys are: " + this.charts.keySet());
                Map<String, byte[][]> chart = this.charts.get(startTime);

                if (chart == null) {
                    this.charts.put(startTime, chart = getScenarioController().createCharts(startTime));
                    LOG.debug("Put chart on cache for id: " + startTime + ", chart == null? " + chart == null);
                    LOG.debug(" - Chart keys are: " + this.charts.keySet());
                }

                execution.setCharts(chart);
            }

            return execution;
        }

        catch (NamingException e) {
            throw new RuntimeException("Can't query stats without a scenario deployed.", e);
        }

        finally {
            this.charting = false;
        }
    }

    private ScenarioController getScenarioController() throws NamingException {

        return (ScenarioController) new InitialContext().lookup(ScenarioController.BINDING);
    }
}
