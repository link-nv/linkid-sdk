/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.ECHO;
import static net.link.safeonline.appconsole.Messages.ECHO_INPUT;
import static net.link.safeonline.appconsole.Messages.ECHO_OUTPUT;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.link.safeonline.sdk.ws.auth.AuthClient;
import net.link.safeonline.sdk.ws.auth.AuthClientImpl;

/**
 * Panel to test out the SafeOnline echo web service
 * 
 * @author wvdhaute
 * 
 */
public class EchoService extends JPanel {

	private static final long serialVersionUID = 1L;

	/*
	 * Actions
	 */
	private Action echoAction = new EchoAction(ECHO.getMessage());
	private Action cancelAction = new CancelAction(CANCEL.getMessage());

	private JTextArea inputMessageArea = new JTextArea();
	private JTextArea returnMessageArea = new JTextArea();

	private ApplicationConsole parent = null;
	private ApplicationConsoleManager consoleManager = null;

	/**
	 * Main constructor.
	 */
	public EchoService(ApplicationConsole applicationConsole) {
		super();
		this.parent = applicationConsole;
		this.consoleManager = ApplicationConsoleManager.getInstance();
		buildWindow();
	}

	private void buildWindow() {
		JScrollPane inScrollPane = new JScrollPane(inputMessageArea);
		inScrollPane.setBorder(new TitledBorder(ECHO_INPUT.getMessage()));
		returnMessageArea.setEditable(false);
		JScrollPane outScrollPane = new JScrollPane(returnMessageArea);
		outScrollPane.setBorder(new TitledBorder(ECHO_OUTPUT.getMessage()));

		JSplitPane infoPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				inScrollPane, outScrollPane);
		infoPanel.setDividerSize(3);
		infoPanel.setResizeWeight(0.5);

		JButton echoButton = new JButton(echoAction);
		echoButton.setMultiClickThreshhold(500);
		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(echoButton);
		controlPanel.add(new JButton(cancelAction));

		JSplitPane fullPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				infoPanel, controlPanel);
		fullPanel.setDividerSize(3);
		fullPanel.setResizeWeight(1.0);
		
		this.setLayout(new GridLayout());
		this.add(fullPanel);
	}

	private class EchoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public EchoAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Echo");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			String message = inputMessageArea.getText();
			AuthClient authClient = new AuthClientImpl(consoleManager
					.getLocation());
			returnMessageArea.setText(authClient.echo(message));
		}
	}

	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CancelAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Cancel");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			parent.resetContent();
		}
	}

}
