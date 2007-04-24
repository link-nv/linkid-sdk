/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import org.apache.commons.logging.Log;

/**
 * Applet View interface. This interface represents the view (as in MVC) of the
 * applet. This view interface is defined independent from the used GUI
 * technology.
 * 
 * @author fcorneli
 * 
 */
public interface AppletView {

	/**
	 * Outputs an information message via the view component.
	 * 
	 * @param infoLevel
	 * @param message
	 */
	void outputInfoMessage(final InfoLevel infoLevel, final String message);

	void outputDetailMessage(final String message);

	/**
	 * Gives back a logger. The logger can manage it's output via the view.
	 * 
	 * @return
	 */
	Log getLog();
}
