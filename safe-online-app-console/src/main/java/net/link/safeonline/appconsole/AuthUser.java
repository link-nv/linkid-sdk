package net.link.safeonline.appconsole;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import static net.link.safeonline.appconsole.Messages.AUTH_USER;
import static net.link.safeonline.appconsole.Messages.CANCEL;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;

public class AuthUser extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;

	private ApplicationConsole parent = null;

	/*
	 * Actions
	 */
	private Action authAction = new AuthAction(AUTH_USER.getMessage());
	private Action cancelAction = new CancelAction(CANCEL.getMessage());

	/*
	 * View
	 */
	private ButtonGroup protocolButtonGroup = new ButtonGroup();
	private JRadioButton[] protocolButtons = null;
	private JTextField applicationField = new JTextField(10);
	private JTextField urlField = new JTextField(10);

	public AuthUser(ApplicationConsole applicationConsole) {
		this.parent = applicationConsole;
		setupProtocolButtons();
		buildWindow();
	}

	private void setupProtocolButtons() {
		AuthenticationProtocol[] protocols = AuthenticationProtocol.values();
		protocolButtons = new JRadioButton[protocols.length];
		for (int i = 0; i < protocols.length; i++) {
			protocolButtons[i] = new JRadioButton(protocols[i].toString());
			protocolButtonGroup.add(protocolButtons[i]);
		}
	}

	private void buildWindow() {
		JLabel applicationLabel = new JLabel("Application");
		JLabel urlLabel = new JLabel("URL");

		JPanel infoPanel = new JPanel();

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		infoPanel.setLayout(gbl);
		
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = 1;
		gbc.insets = new Insets(5, 2, 5, 2);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(applicationLabel, gbc);
		infoPanel.add(applicationLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbl.setConstraints(applicationField, gbc);
		infoPanel.add(applicationField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbl.setConstraints(urlLabel, gbc);
		infoPanel.add(urlLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbl.setConstraints(urlField, gbc);
		infoPanel.add(urlField, gbc);

		int gridy = 2;
		for ( JRadioButton protocolButton : protocolButtons ) {
			JLabel protocolLabel = new JLabel("Protocol");
			
			gbc.gridx = 0;
			gbc.gridy = gridy;
			gbl.setConstraints(protocolLabel, gbc);
			infoPanel.add(protocolLabel, gbc);

			gbc.gridx = 1;
			gbc.gridy = gridy;
			gbl.setConstraints(protocolButton, gbc);
			infoPanel.add(protocolButton, gbc);
			
			gridy++;
		}
		
		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(new JButton(authAction));
		controlPanel.add(new JButton(cancelAction));

		this.setLayout(new BorderLayout());
		this.add(infoPanel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);

	}

	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	public class AuthAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AuthAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Authenticate User");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
		}

		public void actionPerformed(ActionEvent evt) {
		}
	}

	public class CancelAction extends AbstractAction {

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
