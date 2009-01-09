/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * <h2>{@link ScenarioExecution}<br>
 * <sub>A data structure that holds the results of a scenario execution.</sub></h2>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ScenarioExecution implements Serializable, Comparable<ScenarioExecution>, Cloneable {

    private static final long     serialVersionUID = 1L;

    private Map<String, byte[][]> charts;
    private String                hostname;
    private Boolean               ssl;
    private Double                speed;
    private Long                  duration;
    private Integer               workers;
    private Integer               agents;
    private String                scenarioName;
    private String                scenarioDescription;
    private Date                  startTime;


    public ScenarioExecution(String scenarioName, String scenarioDescription, Integer agents, Integer workers, Date startTime,
                             Long duration, String hostname, Boolean useSsl, Double speed) {

        this.scenarioName = scenarioName;
        this.scenarioDescription = scenarioDescription;
        this.agents = agents;
        this.workers = workers;
        this.startTime = startTime;
        this.duration = duration;
        this.hostname = hostname;
        ssl = useSsl;
        this.speed = speed;
    }

    public Map<String, byte[][]> getCharts() {

        return charts;
    }

    public void setCharts(Map<String, byte[][]> charts) {

        this.charts = charts;
    }

    public String getHostname() {

        return hostname;
    }

    public Boolean isSsl() {

        return ssl;
    }

    public Double getSpeed() {

        return speed;
    }

    public Long getDuration() {

        return duration;
    }

    public Integer getWorkers() {

        return workers;
    }

    public Integer getAgents() {

        return agents;
    }

    public String getScenarioName() {

        return scenarioName;
    }

    public String getScenarioDescription() {

        return scenarioDescription;
    }

    public Date getStartTime() {

        return startTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String formattedStartTime = null;
        if (startTime != null) {
            formattedStartTime = new SimpleDateFormat("HH:mm").format(startTime);
        }

        return String.format("%s: [%s] %sx%s (%s min): %s #/s", scenarioName == null? "N/A": scenarioName.replaceFirst(".*\\.",
                ""), formattedStartTime == null? "N/A": formattedStartTime, agents == null? "N/A": agents,
                workers == null? "N/A": workers, duration == null? "N/A": duration / 60000, speed == null? "N/A"
                        : String.format("%.2f", speed));
    }

    /**
     * <b>NOTE</b>: The clone will <b>NOT</b> contain no charts even if this instance does!
     * 
     * {@inheritDoc}
     */
    @Override
    public ScenarioExecution clone() {

        return new ScenarioExecution(scenarioName, scenarioDescription, agents, workers, startTime, duration,
                hostname, ssl, speed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ScenarioExecution))
            return false;
        if (o == this)
            return true;

        ScenarioExecution other = (ScenarioExecution) o;
        return startTime.equals(other.startTime) && hashCode() == other.hashCode();
    }

    /**
     * @return <code>true</code> If this execution and the other execution were executed in the same execution request by the console.
     */
    public boolean equalRequest(ScenarioExecution o) {

        return startTime.equals(o.startTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        double hashCode = startTime.hashCode();
        if (scenarioName != null) {
            hashCode += scenarioName.hashCode();
        }
        if (hostname != null) {
            hashCode += hostname.hashCode();
        }
        if (duration != null) {
            hashCode += duration.hashCode();
        }
        if (workers != null) {
            hashCode += workers.hashCode();
        }
        if (agents != null) {
            hashCode += agents.hashCode();
        }
        if (speed != null) {
            hashCode += speed.hashCode();
        }

        // Our hash is the integer average of all hashes.
        hashCode /= 7;
        if ((int) hashCode == 0) {
            hashCode = Math.signum(hashCode);
        }

        return (int) hashCode;
    }

    /**
     * {@inheritDoc}
     * 
     * Assure contract with equals; for {@link ScenarioExecution}s with the same startTime (execution id) compare hashCode to differentiate
     * other possible differences (like speed).
     */
    public int compareTo(ScenarioExecution o) {

        int difference = startTime.compareTo(o.startTime);
        if (difference == 0) {
            difference = hashCode() - o.hashCode();
        }

        return difference;
    }
}
