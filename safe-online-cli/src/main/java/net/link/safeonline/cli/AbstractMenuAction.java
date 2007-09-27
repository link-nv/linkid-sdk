/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.cli;

public abstract class AbstractMenuAction implements Runnable {

	private final char activationChar;

	private final String description;

	public AbstractMenuAction(char activationChar, String description) {
		this.activationChar = activationChar;
		this.description = description;
	}

	public boolean isActive() {
		return true;
	}

	@Override
	public String toString() {
		return "[" + Character.toUpperCase(this.activationChar) + "] "
				+ this.description;
	}

	public char getActivationChar() {
		return this.activationChar;
	}

	public String getDescription() {
		return this.description;
	}
}
