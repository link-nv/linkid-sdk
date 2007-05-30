/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import javax.swing.JFrame;

import static net.link.safeonline.appconsole.Messages.TITLE;

/**
 * Application Console main frame.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationConsole extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Main constructor.
	 */
	public ApplicationConsole() {
		super(TITLE.getMessage());

		setSize(500, 400);
		setVisible(true);
	}
}
