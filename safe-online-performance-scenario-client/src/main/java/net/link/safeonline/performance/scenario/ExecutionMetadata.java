/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.scenario;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * <h2>{@link ExecutionMetadata}<br>
 * <sub>Holds execution metadata for communication between the agent and the scenario.</sub></h2>
 * 
 * <p>
 * This object can be used by the agent for making a scenario execution request to the {@link ScenarioController}. In this case, use
 * {@link #createRequest(String, Integer, Integer, Date, Long, String, Boolean)} to obtain the object. Only the fields required for making a
 * request will be filled in, the others will remain <code>null</code>.<br>
 * <br>
 * This object can also be used by the scenario to describe a previously completed scenario execution on request of the agent. In this case,
 * use {@link #createResponse(String, String, Integer, Integer, Date, Long, String, Boolean, Double)} . All available fields can be set
 * providing the agent with as much information about the result of the execution as available.
 * </p>
 * 
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ExecutionMetadata {

    private Integer agents;
    private Integer workers;
    private Date    startTime;
    private Long    duration;
    private String  hostname;
    private Boolean ssl;
    private Double  speed;
    private String  scenarioName;
    private String  scenarioDescription;


    /**
     * Use this constructor to create an execution initiation request.
     */
    public static ExecutionMetadata createRequest(String scenarioName, Integer agents, Integer workers, Date startTime, Long duration,
                                                  String hostname, Boolean useSsl) {

        return new ExecutionMetadata(scenarioName, null, agents, workers, startTime, duration, hostname, useSsl, null);
    }

    /**
     * Use this constructor to create an execution result response.
     */
    public static ExecutionMetadata createResponse(String scenarioName, String scenarioDescription, Integer agents, Integer workers,
                                                   Date startTime, Long duration, String hostname, Boolean useSsl, Double speed) {

        return new ExecutionMetadata(scenarioName, scenarioDescription, agents, workers, startTime, duration, hostname, useSsl, speed);
    }

    /**
     * Complete constructor.
     */
    private ExecutionMetadata(String scenarioName, String scenarioDescription, Integer agents, Integer workers, Date startTime,
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

    public String getScenarioName() {

        return scenarioName;
    }

    public String getScenarioDescription() {

        return scenarioDescription;
    }

    public void setScenarioName(String scenarioName) {

        this.scenarioName = scenarioName;
    }

    public Integer getAgents() {

        return agents;
    }

    public Integer getWorkers() {

        return workers;
    }

    public Date getStartTime() {

        return startTime;
    }

    public Long getDuration() {

        return duration;
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
}
