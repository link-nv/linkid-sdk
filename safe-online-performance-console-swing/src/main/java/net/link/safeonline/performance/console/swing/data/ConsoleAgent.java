/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.NameNotFoundException;
import javax.swing.SwingUtilities;

import net.link.safeonline.performance.console.ScenarioExecution;
import net.link.safeonline.performance.console.ScenarioRemoting;
import net.link.safeonline.performance.console.jgroups.Agent;
import net.link.safeonline.performance.console.jgroups.AgentState;
import net.link.safeonline.performance.console.swing.model.ScenarioThread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;


/**
 * <h2>{@link ConsoleAgent}<br>
 * <sub>Proxy that maintains the status of the remote agent and provides access to its functionality.</sub></h2>
 * 
 * <p>
 * This is a proxy for the remote agent and provides access to all functionality offered and all state made available by the agent. It takes
 * care of keeping the status information synchronised by a daemon thread that checks the remote status every two seconds.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ConsoleAgent implements Agent {

    private static final long      serialVersionUID = 1L;
    static final Log               LOG              = LogFactory.getLog(ConsoleAgent.class);

    private ScenarioRemoting       agentRemoting;
    private Address                agentAddress;
    private boolean                healthy;
    private AgentState             transit;
    private AgentState             state;
    private Throwable              error;
    private Set<String>            scenarios;
    private Set<ScenarioExecution> executions;
    public boolean                 autoUpdate;
    private UpdateAgentState       updater;
    private Set<ScenarioThread>    scenarioThreads;


    /**
     * Create a new {@link ConsoleAgent} component based off the agent at the given {@link Address}.
     */
    public ConsoleAgent(Address agentAddress) {

        agentRemoting = ConsoleData.getRemoting();
        this.agentAddress = agentAddress;
        autoUpdate = true;
        healthy = true;

        scenarioThreads = new HashSet<ScenarioThread>();
        updater = new UpdateAgentState();
        updater.start();
    }

    /**
	 */
    public void registerAction(ScenarioThread actionThread) {

        scenarioThreads.add(actionThread);
    }

    /**
	 */
    public void unregisterAction(ScenarioThread actionThread) {

        scenarioThreads.remove(actionThread);
    }

    /**
     * Order the agent to prepare for GC by stopping the update cycle.
     */
    public void shutdown() {

        resetTransit();
        updater.shutdown();
    }

    /**
     * @param autoUpdate
     *            <code>true</code> to sync the stats with the remote agent at a certain interval. <code>false</code> to suspend this
     *            updating (until set to <code>true</code> again).
     */
    public void setAutoUpdate(boolean autoUpdate) {

        this.autoUpdate = autoUpdate;
    }

    /**
     * @return false if JGroups suspects this agent of being unavailable.
     */
    public boolean isHealthy() {

        return healthy;
    }

    /**
     * Set the JGroups health status of this agent.
     */
    public void setHealthy(boolean healthy) {

        this.healthy = healthy;
        ConsoleData.fireAgentStatus(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String transitStr = AgentState.RESET.getTransitioning();
        String stateStr = AgentState.RESET.getTransitioning();
        if (null != transit) {
            transitStr = transit.getTransitioning();
        }
        if (null != state) {
            stateStr = state.getState();
        }

        return String.format("%s: [%s:%s]", agentAddress, stateStr, transitStr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return agentAddress.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ConsoleAgent))
            return false;

        return agentAddress.equals(((ConsoleAgent) obj).agentAddress);
    }

    /**
     * Retrieve the JGroups address of this {@link ConsoleAgent}.
     */
    public Address getAddress() {

        return agentAddress;
    }

    /**
     * Will never be <code>null</code>.
     * 
     * {@inheritDoc}
     */
    public AgentState getState() {

        return state;
    }

    /**
     * {@inheritDoc}
     */
    public void resetTransit() {

        for (ScenarioThread actionThread : scenarioThreads) {
            actionThread.shutdown();
        }

        agentRemoting.resetTransit(agentAddress);
        updateState();
    }

    /**
     * {@inheritDoc}
     */
    public AgentState getTransit() {

        return transit;
    }

    /**
     * @return <code>true</code> if this agent is undertaking an action.
     */
    public boolean isTransitting() {

        return transit != null && !AgentState.RESET.equals(transit);
    }

    /**
     * <b>Temporarily</b> change the <b>local</b> transition state of the agent.<br>
     * <br>
     * You should only use this to set the transition state just before making a request to the remote agent that will result in the same
     * state transition if all goes well in order to have the state reflected in the UI sooner.
     */
    public void setTransit(AgentState transit) {

        notifyOnChange(this.transit, this.transit = transit);
    }

    /**
     * <b>Temporarily</b> change the <b>local</b> error of the agent.<br>
     * <br>
     * You should only use this to set the transition state just before making a request to the remote agent that will result in the same
     * state transition if all goes well in order to have the state reflected in the UI sooner.
     */
    public void setError(Throwable error) {

        notifyOnChange(this.error, this.error = error);
    }

    /**
     * {@inheritDoc}
     */
    public Throwable getError() {

        return error;
    }

    /**
     * {@inheritDoc}
     */
    public ScenarioExecution getCharts(Date startTime) {

        return agentRemoting.getCharts(agentAddress, startTime);
    }

    /**
     * {@inheritDoc}
     */
    public Set<ScenarioExecution> getExecutions() {

        return executions;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getScenarios() {

        return scenarios;
    }

    public void updateState() {

        if (SwingUtilities.isEventDispatchThread()) {
            LOG.warn("We're in the event queue!  Deadlock may occur.");
        }

        try {
            boolean isDeployed = state != null && AgentState.UPLOAD.compareTo(state) < 0;

            synchronized (ConsoleData.lock) {
                transit = notifyOnChange(transit, agentRemoting.getTransit(agentAddress));
            }
            synchronized (ConsoleData.lock) {
                state = notifyOnChange(state, agentRemoting.getState(agentAddress));
            }
            synchronized (ConsoleData.lock) {
                error = notifyOnChange(error, agentRemoting.getError(agentAddress));
            }
            synchronized (ConsoleData.lock) {
                scenarios = notifyOnChange(scenarios, isDeployed? agentRemoting.getScenarios(agentAddress): null);
            }
            synchronized (ConsoleData.lock) {
                executions = notifyOnChange(executions, isDeployed? agentRemoting.getExecutions(agentAddress): null);
            }
        }

        catch (IllegalStateException e) {
            LOG.debug(e.getMessage());
            state = notifyOnChange(state, null);
            transit = notifyOnChange(transit, null);
            error = notifyOnChange(error, null);
            scenarios = notifyOnChange(scenarios, null);
            executions = notifyOnChange(executions, null);
        }

        catch (NameNotFoundException e) {
            scenarios = notifyOnChange(scenarios, null);
            executions = notifyOnChange(executions, null);
        }

        catch (Throwable e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }

            setError(cause);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V notifyOnChange(V oldValue, V newValue) {

        // Don't accept new value when autoUpdate has been disabled.
        if (Thread.currentThread() instanceof UpdateAgentState && !autoUpdate)
            return oldValue;

        // Equals is broken for non-sorted sets when the order gets shaken up.
        Object fixedOld = oldValue, fixedNew = newValue;
        if (oldValue instanceof Set && !(oldValue instanceof SortedSet)) {
            fixedOld = new TreeSet<Object>((Set<? extends Object>) oldValue);
        }
        if (newValue instanceof Set && !(newValue instanceof SortedSet)) {
            fixedNew = new TreeSet<Object>((Set<? extends Object>) newValue);
        }

        if (fixedOld != null) {
            if (fixedNew == null || !fixedOld.equals(fixedNew)) {
                ConsoleData.fireAgentStatus(this);
            }
        }

        else if (fixedNew != null) {
            ConsoleData.fireAgentStatus(this);
        }

        return newValue;
    }


    private class UpdateAgentState extends Thread {

        private static final long INTERVAL = 2000;
        private boolean           shutdown;


        public UpdateAgentState() {

            setDaemon(true);
        }

        protected void shutdown() {

            shutdown = true;
            interrupt();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            while (!shutdown) {
                try {
                    if (autoUpdate) {
                        updateState();
                    }

                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
