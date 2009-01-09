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
package net.link.safeonline.performance.service.bean;

import java.util.LinkedList;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.performance.entity.ScenarioTimingEntity;
import net.link.safeonline.performance.service.ScenarioTimingService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link ScenarioTimingServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 3, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = ScenarioTimingService.JNDI_BINDING)
public class ScenarioTimingServiceBean extends AbstractProfilingServiceBean implements ScenarioTimingService {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public LinkedList<ScenarioTimingEntity> getExecutionTimings(ExecutionEntity execution, int dataPoints) {

        // Find the execution's profile data.
        Long dataDuration = (Long) em.createNamedQuery(ScenarioTimingEntity.getExecutionDuration).setParameter("execution", execution)
                                          .getSingleResult();
        Long dataStart = (Long) em.createNamedQuery(ScenarioTimingEntity.getExecutionStart).setParameter("execution", execution)
                                       .getSingleResult();

        // Bail out of there is no data for this execution.
        if (dataDuration == null || dataStart == null || dataDuration + dataStart == 0) {
            LOG.warn("No data for execution: " + execution.getStartTime());
            return new LinkedList<ScenarioTimingEntity>();
        }

        int period = (int) Math.ceil((double) dataDuration / dataPoints);

        LinkedList<ScenarioTimingEntity> pointData = new LinkedList<ScenarioTimingEntity>();
        for (long point = 0; point * period < dataDuration; ++point) {
            try {
                pointData.add((ScenarioTimingEntity) em.createNamedQuery(ScenarioTimingEntity.createAverage).setParameter("execution",
                        execution).setParameter("start", dataStart + point * period).setParameter("stop", dataStart + (point + 1) * period)
                                                            .getSingleResult());
            } catch (NoResultException e) {
            }
        }

        return pointData;
    }
}
