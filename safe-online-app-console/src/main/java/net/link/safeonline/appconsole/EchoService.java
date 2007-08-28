package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.ECHO;
import static net.link.safeonline.appconsole.Messages.ECHO_INPUT;
import static net.link.safeonline.appconsole.Messages.ECHO_OUTPUT;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.link.safeonline.sdk.ws.auth.AuthClient;
import net.link.safeonline.sdk.ws.auth.AuthClientImpl;

public class EchoService extends JPanel {

	private static final long serialVersionUID = 1L;

	/*
	 * Actions
	 */
	private Action echoAction = new EchoAction(ECHO.getMessage());
	private Action cancelAction = new CancelAction(CANCEL.getMessage());

	private JTextField inputMessageField = new JTextField(20);
	private JTextField returnMessageField = new JTextField(20);

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
		JPanel infoPanel = new JPanel();
		JPanel controlPanel = new JPanel();

		JLabel inputMessageLabel = new JLabel(ECHO_INPUT.getMessage());
		JLabel returnMessageLabel = new JLabel(ECHO_OUTPUT.getMessage());

		// infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		infoPanel.add(inputMessageLabel);
		infoPanel.add(inputMessageField);
		infoPanel.add(returnMessageLabel);
		infoPanel.add(returnMessageField);
		returnMessageField.setEditable(false);

		JButton echoButton = new JButton(echoAction);
		echoButton.setMultiClickThreshhold(500);
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(echoButton);
		controlPanel.add(new JButton(cancelAction));

		this.setLayout(new BorderLayout());
		this.add(infoPanel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	private class EchoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public EchoAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Echo");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			String message = inputMessageField.getText();
			AuthClient authClient = new AuthClientImpl(consoleManager
					.getLocation());
			returnMessageField.setText(authClient.echo(message));
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
