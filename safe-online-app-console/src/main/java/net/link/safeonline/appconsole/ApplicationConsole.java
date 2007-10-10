/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.ATTRIB;
import static net.link.safeonline.appconsole.Messages.AUTH_USER;
import static net.link.safeonline.appconsole.Messages.CAPTURE;
import static net.link.safeonline.appconsole.Messages.CREATE_P12;
import static net.link.safeonline.appconsole.Messages.EXTRACT_CERT;
import static net.link.safeonline.appconsole.Messages.FILE;
import static net.link.safeonline.appconsole.Messages.LOAD_IDENTITY;
import static net.link.safeonline.appconsole.Messages.QUIT;
import static net.link.safeonline.appconsole.Messages.SERVICES;
import static net.link.safeonline.appconsole.Messages.SET_LOCATION;
import static net.link.safeonline.appconsole.Messages.TITLE;
import static net.link.safeonline.appconsole.Messages.UTILS;
import static net.link.safeonline.appconsole.Messages.VIEW_INBOUND_SOAP;
import static net.link.safeonline.appconsole.Messages.VIEW_OUTBOUND_SOAP;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.xml.transform.TransformerException;

import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.ws.MessageAccessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * Application Console main frame.
 * 
 * @author fcorneli
 * 
 */
public class ApplicationConsole extends JFrame implements Observer {

	private static final long serialVersionUID = 1L;

	static final Log LOG = LogFactory.getLog(ApplicationConsole.class);

	/*
	 * Actions
	 */
	private Action loadIdAction = new LoadIdAction(LOAD_IDENTITY.getMessage());
	private Action createP12Action = new CreateP12Action(CREATE_P12
			.getMessage());
	private Action extractAction = new ExtractAction(EXTRACT_CERT.getMessage());
	private Action setLocationAction = new SetLocationAction(SET_LOCATION
			.getMessage());
	private Action quitAction = new QuitAction(QUIT.getMessage());

	private Action attribAction = new AttribAction(ATTRIB.getMessage());

	private Action authUserAction = new AuthUserAction(AUTH_USER.getMessage());

	private Action captureAction = new CaptureAction(CAPTURE.getMessage());
	Action viewInboundAction = new ViewInboundAction(VIEW_INBOUND_SOAP
			.getMessage());
	Action viewOutboundAction = new ViewOutboundAction(VIEW_OUTBOUND_SOAP
			.getMessage());

	/*
	 * GUI components
	 */
	private JPanel contentPanel = null;
	private JPanel debugPanel = null;
	private JSplitPane splitPane = null;
	private JSplitPane statusPanel = null;

	private JLabel identityLabel = new JLabel();
	private JLabel locationLabel = new JLabel();

	private JComboBox browserCombo = new JComboBox();

	/*
	 * Menus
	 */
	private JMenu fileMenu = new JMenu(FILE.getMessage());
	private JMenu servicesMenu = new JMenu(SERVICES.getMessage());
	private JMenu utilsMenu = new JMenu(UTILS.getMessage());

	private JMenuItem loadIdentityMenuItem = new JMenuItem(this.loadIdAction);
	private JMenuItem createP12MenuItem = new JMenuItem(this.createP12Action);
	private JMenuItem extractCertMenuItem = new JMenuItem(this.extractAction);
	private JMenuItem setLocationMenuItem = new JMenuItem(
			this.setLocationAction);
	private JMenuItem authUserMenuItem = new JMenuItem(this.authUserAction);
	private JMenuItem quitMenuItem = new JMenuItem(this.quitAction);

	private JMenuItem attribMenuItem = new JMenuItem(this.attribAction);

	private JMenuItem[] servicesMenuItems = { this.attribMenuItem };

	/*
	 * Non-GUI members
	 */
	public ApplicationConsoleManager consoleManager = ApplicationConsoleManager
			.getInstance();
	private BrowserLauncher browserLauncher = null;

	/**
	 * Main constructor.
	 */
	public ApplicationConsole() {
		super(TITLE.getMessage());

		buildMenu();
		buildWindow();

		manageServices();
		setStatus();

		this.viewInboundAction.setEnabled(false);
		this.viewOutboundAction.setEnabled(false);
		this.consoleManager.addObserver(this);
		ApplicationConsoleManager.getInstance().addObserver(this);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(750, 600);
		this.setLocation(50, 50);
		this.setVisible(true);
	}

	private void buildMenu() {
		this.fileMenu.setMnemonic(KeyEvent.VK_F);
		this.servicesMenu.setMnemonic(KeyEvent.VK_S);
		this.utilsMenu.setMnemonic(KeyEvent.VK_U);

		this.fileMenu.add(this.loadIdentityMenuItem);
		this.fileMenu.add(this.setLocationMenuItem);
		this.fileMenu.addSeparator();
		this.fileMenu.add(this.authUserMenuItem);
		this.fileMenu.addSeparator();
		this.fileMenu.add(this.quitMenuItem);

		for (JMenuItem m : this.servicesMenuItems)
			this.servicesMenu.add(m);

		this.utilsMenu.add(this.createP12MenuItem);
		this.utilsMenu.add(this.extractCertMenuItem);

		JMenuBar menu = new JMenuBar();
		menu.add(this.fileMenu);
		menu.add(this.servicesMenu);
		menu.add(this.utilsMenu);
		this.setJMenuBar(menu);
	}

	private void buildWindow() {
		buildStatusPanel();
		buildDebugPanel();

		if (this.contentPanel == null)
			this.contentPanel = new JPanel();
		JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				this.debugPanel, this.statusPanel);
		bottomPanel.setDividerSize(3);
		bottomPanel.setResizeWeight(0.5);

		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				this.contentPanel, bottomPanel);
		this.splitPane.setResizeWeight(1.0);
		this.splitPane.setDividerSize(3);

		this.add(this.splitPane);
	}

	private void buildDebugPanel() {
		this.debugPanel = new JPanel(new FlowLayout());

		try {
			this.browserLauncher = new BrowserLauncher();
		} catch (BrowserLaunchingInitializingException e) {
			LOG.error("BrowserLaunchingInitializingException thrown ...", e);
		} catch (UnsupportedOperatingSystemException e) {
			LOG.error("UnsupportedOperatingSystemException thrown ...", e);
		}
		@SuppressWarnings("unchecked")
		List<String> browsers = this.browserLauncher.getBrowserList();
		for (String browser : browsers) {
			this.browserCombo.addItem(browser);
		}
		JCheckBox captureBox = new JCheckBox(this.captureAction);
		captureBox.setSelected(true);
		JLabel browserLabel = new JLabel("Browser");

		this.debugPanel.add(browserLabel);
		this.debugPanel.add(this.browserCombo);
		this.debugPanel.add(captureBox);
		this.debugPanel.add(new JButton(this.viewInboundAction));
		this.debugPanel.add(new JButton(this.viewOutboundAction));
	}

	private void buildStatusPanel() {
		JPanel identityPanel = new JPanel();
		identityPanel.add(this.identityLabel);

		JPanel locationPanel = new JPanel();
		locationPanel.add(this.locationLabel);

		this.statusPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				identityPanel, locationPanel);
		this.statusPanel.setDividerSize(0);
		this.statusPanel.setResizeWeight(0.5);
	}

	protected void onAttribService() {
		this.contentPanel = new AttribService(this);
		this.splitPane.setTopComponent(this.contentPanel);
	}

	protected void onSetLocation() {
		String location = JOptionPane.showInputDialog(
				SET_LOCATION.getMessage(), this.consoleManager.getLocation());
		if (null != location)
			this.consoleManager.setLocation(location);
	}

	protected void onExtract() {
		this.contentPanel = new ExtractCertificate(this);
		this.splitPane.setTopComponent(this.contentPanel);
	}

	protected void onCreate() {
		this.contentPanel = new CreateP12(this);
		this.splitPane.setTopComponent(this.contentPanel);
	}

	protected void onLoad() {
		this.contentPanel = new LoadIdentity(this);
		this.splitPane.setTopComponent(this.contentPanel);
	}

	protected void onAuthUser() {
		this.contentPanel = new AuthUser(this);
		this.splitPane.setTopComponent(this.contentPanel);
	}

	public void resetContent() {
		this.contentPanel = new JPanel();
		this.splitPane.setTopComponent(this.contentPanel);
	}

	public void setStatus() {
		this.identityLabel.setText(this.consoleManager.getIdentityLabel());
		this.locationLabel.setText(this.consoleManager.getLocationLabel());
	}

	private void manageServices() {
		this.attribAction.setEnabled(null != this.consoleManager.getIdentity());
		this.authUserAction.setEnabled(null != this.consoleManager
				.getIdentity());
	}

	void launchBrowser(Document doc, String prefix) {
		File tmpXmlFile;

		try {
			tmpXmlFile = File.createTempFile(prefix, ".xml");
			FileUtils.writeStringToFile(tmpXmlFile, DomUtils.domToString(doc));
			this.browserLauncher.openURLinBrowser((String) this.browserCombo
					.getSelectedItem(), "file://"
					+ tmpXmlFile.getAbsolutePath());
		} catch (TransformerException e) {
			LOG.error("Failed to transform SOAP document to plain-text", e);
		} catch (IOException e) {
			LOG.error("Failed to write SOAP document to file", e);
		}

	}

	public void update(Observable o, Object arg) {
		if (o instanceof ApplicationConsoleManager) {
			if (arg instanceof MessageAccessor) {
				this.viewInboundAction.setEnabled(true);
				this.viewOutboundAction.setEnabled(true);
			} else {
				setStatus();
				manageServices();
			}
		}
	}

	/*
	 * 
	 * Action classes
	 * 
	 */
	public class LoadIdAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public LoadIdAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onLoad();
		}
	}

	public class CreateP12Action extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CreateP12Action(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onCreate();
		}
	}

	public class ExtractAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ExtractAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onExtract();
		}
	}

	public class SetLocationAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SetLocationAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onSetLocation();
		}
	}

	public class QuitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public QuitAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			LOG.info("Closing Swing SafeOnline Application Console...");
			System.exit(0);
		}
	}

	public class AttribAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public AttribAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onAttribService();
		}
	}

	public class AuthUserAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public AuthUserAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			onAuthUser();
		}
	}

	public class ViewInboundAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewInboundAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			Document inboundSoap = ApplicationConsoleManager.getInstance()
					.getInboundMessage();
			launchBrowser(inboundSoap, "inbound_soap");
		}
	}

	public class ViewOutboundAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ViewOutboundAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}

		public void actionPerformed(@SuppressWarnings("unused")
		ActionEvent evt) {
			Document outboundSoap = ApplicationConsoleManager.getInstance()
					.getOutboundMessage();
			launchBrowser(outboundSoap, "outbound_soap");
		}
	}

	public class CaptureAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CaptureAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() instanceof JCheckBox) {
				JCheckBox value = (JCheckBox) evt.getSource();
				ApplicationConsoleManager.getInstance().setCaptureMessages(
						value.isSelected());
				ApplicationConsole.this.viewInboundAction.setEnabled(value
						.isSelected());
				ApplicationConsole.this.viewOutboundAction.setEnabled(value
						.isSelected());
			}
		}
	}

}
