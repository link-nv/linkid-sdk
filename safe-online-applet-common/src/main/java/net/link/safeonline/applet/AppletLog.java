/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import org.apache.commons.logging.Log;

/**
 * Implementation of Logger using the applet view as output.
 * 
 * @author fcorneli
 * 
 */
public class AppletLog implements Log {

	private final AppletView appletView;

	public AppletLog(final AppletView appletView) {
		this.appletView = appletView;
	}

	public void debug(Object message) {
		this.appletView.outputDetailMessage("DEBUG: " + message);
	}

	public void debug(Object message, Throwable t) {
		this.appletView.outputDetailMessage("DEBUG: " + message);
	}

	public void error(Object message) {
		this.appletView.outputDetailMessage("ERROR: " + message);
	}

	public void error(Object message, Throwable t) {
		this.appletView.outputDetailMessage("ERROR: " + message);
	}

	public void fatal(Object message) {
		this.appletView.outputDetailMessage("FATAL: " + message);
	}

	public void fatal(Object message, Throwable t) {
		this.appletView.outputDetailMessage("FATAL: " + message);
	}

	public void info(Object message) {
		this.appletView.outputDetailMessage("INFO: " + message);
	}

	public void info(Object message, Throwable t) {
		this.appletView.outputDetailMessage("INFO: " + message);
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	public boolean isFatalEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isTraceEnabled() {
		return true;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public void trace(Object message) {
		this.appletView.outputDetailMessage("TRACE: " + message);
	}

	public void trace(Object message, Throwable t) {
		this.appletView.outputDetailMessage("TRACE: " + message);
	}

	public void warn(Object message) {
		this.appletView.outputDetailMessage("WARN: " + message);
	}

	public void warn(Object message, Throwable t) {
		this.appletView.outputDetailMessage("WARN: " + message);
	}
}
