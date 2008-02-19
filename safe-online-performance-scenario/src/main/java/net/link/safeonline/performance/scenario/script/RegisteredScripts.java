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
package net.link.safeonline.performance.scenario.script;

import java.util.HashSet;
import java.util.Set;

import net.link.safeonline.performance.scenario.Scenario;

import org.jfree.util.Log;

/**
 * <h2>{@link RegisteredScripts}<br>
 * <sub>This object maintains a list of known scenarios.</sub></h2>
 *
 * <p>
 * Whenever you add a scenario to this project, you should register it with this
 * object in the static block to make it visible to the console.
 * </p>
 *
 * <p>
 * <i>Feb 19, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class RegisteredScripts {

	private static Set<Class<? extends Scenario>> registeredScenarios = new HashSet<Class<? extends Scenario>>();

	static {
		register(BasicScenario.class);
		register(DummyScenario.class);
	}

	private static void register(Class<? extends Scenario> scenario) {

		Log.debug("registering " + scenario);
		registeredScenarios.add(scenario);
	}

	/**
	 * @return The registered scenarios.
	 */
	public static Set<Class<? extends Scenario>> getRegisteredScenarios() {

		Log.debug("registered: " + registeredScenarios);
		return registeredScenarios;
	}
}
