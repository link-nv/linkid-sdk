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

import java.util.List;

import javax.ejb.Stateless;

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
@LocalBinding(jndiBinding = ScenarioTimingService.BINDING)
public class ScenarioTimingServiceBean extends ProfilingServiceBean implements
		ScenarioTimingService {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<ScenarioTimingEntity> getExecutionTimings(
			ExecutionEntity execution) {

		return this.em.createNamedQuery(ScenarioTimingEntity.getTimings)
				.setParameter("execution", execution).getResultList();
	}
}
