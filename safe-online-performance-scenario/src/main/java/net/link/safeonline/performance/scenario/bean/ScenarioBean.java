/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario.bean;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.performance.scenario.ScenarioLocal;

import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = "SafeOnline/ScenarioBean")
public class ScenarioBean implements ScenarioLocal {

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void execute(Scenario scenario) throws Exception {

		scenario.execute();
	}
}
