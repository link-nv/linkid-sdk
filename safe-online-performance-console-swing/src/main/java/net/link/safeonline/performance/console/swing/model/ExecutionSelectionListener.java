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
package net.link.safeonline.performance.console.swing.model;

import net.link.safeonline.performance.console.ScenarioExecution;


/**
 * <h2>{@link ExecutionSelectionListener}<br>
 * <sub>A listener that is triggered when an execution is selected in the interface.</sub></h2>
 *
 * <p>
 * <i>Feb 14, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public interface ExecutionSelectionListener {

    public void executionSelected(ScenarioExecution execution);
}
