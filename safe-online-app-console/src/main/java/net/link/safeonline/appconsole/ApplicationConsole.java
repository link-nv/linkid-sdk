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
import static net.link.safeonline.appconsole.Messages.ECHO;
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

	private static final Log LOG = LogFactory.getLog(ApplicationConsole.class);

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

	private Action echoAction = new EchoAction(ECHO.getMessage());
	private Action attribAction = new AttribAction(ATTRIB.getMessage());

	private Action authUserAction = new AuthUserAction(AUTH_USER.getMessage());

	private Action captureAction = new CaptureAction(CAPTURE.getMessage());
	private Action viewInboundAction = new ViewInboundAction(VIEW_INBOUND_SOAP
			.getMessage());
	private Action viewOutboundAction = new ViewOutboundAction(
			VIEW_OUTBOUND_SOAP.getMessage());

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

	private JMenuItem loadIdentityMenuItem = new JMenuItem(loadIdAction);
	private JMenuItem createP12MenuItem = new JMenuItem(createP12Action);
	private JMenuItem extractCertMenuItem = new JMenuItem(extractAction);
	private JMenuItem setLocationMenuItem = new JMenuItem(setLocationAction);
	private JMenuItem authUserMenuItem = new JMenuItem(authUserAction);
	private JMenuItem quitMenuItem = new JMenuItem(quitAction);

	private JMenuItem echoMenuItem = new JMenuItem(echoAction);
	private JMenuItem attribMenuItem = new JMenuItem(attribAction);

	private JMenuItem[] servicesMenuItems = { echoMenuItem, attribMenuItem };

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

		viewInboundAction.setEnabled(false);
		viewOutboundAction.setEnabled(false);
		consoleManager.addObserver(this);
		ApplicationConsoleManager.getInstance().addObserver(this);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(750, 600);
		this.setLocation(50, 50);
		this.setVisible(true);
	}

	private void buildMenu() {
		fileMenu.setMnemonic(KeyEvent.VK_F);
		servicesMenu.setMnemonic(KeyEvent.VK_S);
		utilsMenu.setMnemonic(KeyEvent.VK_U);

		fileMenu.add(loadIdentityMenuItem);
		fileMenu.add(setLocationMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(authUserMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(quitMenuItem);

		for (JMenuItem m : servicesMenuItems)
			servicesMenu.add(m);

		utilsMenu.add(createP12MenuItem);
		utilsMenu.add(extractCertMenuItem);

		JMenuBar menu = new JMenuBar();
		menu.add(fileMenu);
		menu.add(servicesMenu);
		menu.add(utilsMenu);
		this.setJMenuBar(menu);
	}

	private void buildWindow() {
		buildStatusPanel();
		buildDebugPanel();

		if (contentPanel == null)
			contentPanel = new JPanel();
		JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				debugPanel, statusPanel);
		bottomPanel.setDividerSize(3);
		bottomPanel.setResizeWeight(0.5);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPanel,
				bottomPanel);
		splitPane.setResizeWeight(1.0);
		splitPane.setDividerSize(3);

		this.add(splitPane);
	}

	private void buildDebugPanel() {
		debugPanel = new JPanel(new FlowLayout());

		try {
			browserLauncher = new BrowserLauncher();
		} catch (BrowserLaunchingInitializingException e) {
			LOG.error("BrowserLaunchingInitializingException thrown ...", e);
		} catch (UnsupportedOperatingSystemException e) {
			LOG.error("UnsupportedOperatingSystemException thrown ...", e);
		}
		@SuppressWarnings("unchecked")
		List<String> browsers = browserLauncher.getBrowserList();
		for (String browser : browsers) {
			browserCombo.addItem(browser);
		}
		JCheckBox captureBox = new JCheckBox(captureAction);
		captureBox.setSelected(true);
		JLabel browserLabel = new JLabel("Browser");

		debugPanel.add(browserLabel);
		debugPanel.add(browserCombo);
		debugPanel.add(captureBox);
		debugPanel.add(new JButton(viewInboundAction));
		debugPanel.add(new JButton(viewOutboundAction));
	}

	private void buildStatusPanel() {
		JPanel identityPanel = new JPanel();
		identityPanel.add(identityLabel);

		JPanel locationPanel = new JPanel();
		locationPanel.add(locationLabel);

		statusPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				identityPanel, locationPanel);
		statusPanel.setDividerSize(0);
		statusPanel.setResizeWeight(0.5);
	}

	protected void onAttribService() {
		contentPanel = new AttribService(this);
		splitPane.setTopComponent(contentPanel);
	}

	protected void onEchoService() {
		contentPanel = new EchoService(this);
		splitPane.setTopComponent(contentPanel);
	}

	protected void onSetLocation() {
		String location = JOptionPane.showInputDialog(
				SET_LOCATION.getMessage(), consoleManager.getLocation());
		if (null != location)
			consoleManager.setLocation(location);
	}

	protected void onExtract() {
		contentPanel = new ExtractCertificate(this);
		splitPane.setTopComponent(contentPanel);
	}

	protected void onCreate() {
		contentPanel = new CreateP12(this);
		splitPane.setTopComponent(contentPanel);
	}

	protected void onLoad() {
		contentPanel = new LoadIdentity(this);
		splitPane.setTopComponent(contentPanel);
	}

	protected void onAuthUser() {
		contentPanel = new AuthUser(this);
		splitPane.setTopComponent(contentPanel);
	}

	public void resetContent() {
		contentPanel = new JPanel();
		splitPane.setTopComponent(contentPanel);
	}

	public void setStatus() {
		identityLabel.setText(consoleManager.getIdentityLabel());
		locationLabel.setText(consoleManager.getLocationLabel());
	}

	private void manageServices() {
		attribAction.setEnabled(null != consoleManager.getIdentity());
		authUserAction.setEnabled(null != consoleManager.getIdentity());
	}

	private void launchBrowser(Document doc, String prefix) {
		File tmpXmlFile;

		try {
			tmpXmlFile = File.createTempFile(prefix, ".xml");
			FileUtils.writeStringToFile(tmpXmlFile, DomUtils.domToString(doc));
			browserLauncher.openURLinBrowser((String) browserCombo
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
				viewInboundAction.setEnabled(true);
				viewOutboundAction.setEnabled(true);
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

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent evt) {
			LOG.info("Closing Swing SafeOnline Application Console...");
			System.exit(0);
		}
	}

	public class EchoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public EchoAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void actionPerformed(ActionEvent evt) {
			onEchoService();
		}
	}

	public class AttribAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public AttribAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		}

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent arg0) {
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

		public void actionPerformed(ActionEvent evt) {
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

		public void actionPerformed(ActionEvent evt) {
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
				viewInboundAction.setEnabled(value.isSelected());
				viewOutboundAction.setEnabled(value.isSelected());
			}
		}
	}

}
