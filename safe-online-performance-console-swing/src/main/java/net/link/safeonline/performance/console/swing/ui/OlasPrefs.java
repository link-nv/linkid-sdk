/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

import net.link.safeonline.performance.console.swing.data.ConsoleData;

/**
 * This class keeps and listens to the components that contain the location of
 * the OLAS service.
 * 
 * @author mbillemo
 */
public class OlasPrefs extends JPanel implements FocusListener {

	private static final long serialVersionUID = 1L;

	protected JTextField hostname;
	protected JTextField port;
	protected JTextField workers;
	protected ConsoleData consoleData;

	/**
	 * @param consoleData
	 */
	public OlasPrefs(ConsoleData consoleData) {

		this.consoleData = consoleData;

		this.hostname = new JTextField(consoleData.getHostname());
		this.port = new JTextField(String.valueOf(consoleData.getPort()));
		this.workers = new JTextField(String.valueOf(consoleData.getWorkers()));

		this.hostname.addFocusListener(this);
		this.port.addFocusListener(this);
		this.workers.addFocusListener(this);

		this.port.setColumns(5);
		this.workers.setColumns(5);
	}

	/**
	 * @{inheritDoc}
	 */
	public void focusLost(FocusEvent e) {

		if (this.hostname.equals(e.getSource()))
			this.consoleData.setHostname(this.hostname.getText());

		else if (this.port.equals(e.getSource()))
			this.consoleData.setPort(Integer.parseInt(this.port.getText()));

		else if (this.workers.equals(e.getSource()))
			this.consoleData.setWorkers(Integer
					.parseInt(this.workers.getText()));
	}

	/**
	 * @{inheritDoc}
	 */
	public void focusGained(FocusEvent e) {

		focusLost(e);
	}

}
