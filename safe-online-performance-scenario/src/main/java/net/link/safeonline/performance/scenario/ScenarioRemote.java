/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.scenario;

import java.util.List;

import javax.ejb.Remote;

import net.link.safeonline.performance.drivers.DriverException;

import org.jfree.chart.JFreeChart;

/**
 * @author mbillemo
 * 
 */
@Remote
public interface ScenarioRemote {

	public List<JFreeChart> execute(String hostname) throws DriverException;
}
