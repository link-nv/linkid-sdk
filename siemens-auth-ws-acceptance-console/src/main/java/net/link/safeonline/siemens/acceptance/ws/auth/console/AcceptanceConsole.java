/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.siemens.acceptance.ws.auth.console.device.PasswordAuthentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Siemens Acceptance Console main frame.
 * 
 * @author wvdhaute
 * 
 */
public class AcceptanceConsole extends JFrame implements Observer {

    private static final long       serialVersionUID        = 1L;

    static final Log                LOG                     = LogFactory.getLog(AcceptanceConsole.class);

    /*
     * Actions
     */
    private Action                  authPasswordAction      = new AuthPasswordAction("Password ...");

    private Action                  setLocationAction       = new SetLocationAction("Set Location ...");
    private Action                  setApplicationAction    = new SetApplicationAction("Set Application ...");
    private Action                  generateKeyPairAction   = new GenerateKeyPairAction("Generate Keypair ...");

    private Action                  quitAction              = new QuitAction("Quit");

    /*
     * GUI components
     */
    private JPanel                  contentPanel            = null;
    private JSplitPane              splitPane               = null;
    private JSplitPane              statusPanel             = null;

    private JLabel                  locationLabel           = new JLabel();
    private JLabel                  applicationLabel        = new JLabel();

    /*
     * Menus
     */
    private JMenu                   authMenu                = new JMenu("OLAS WS Authentication");

    private JMenuItem               passwordMenuItem        = new JMenuItem(this.authPasswordAction);

    private JMenuItem               setApplicationMenuItem  = new JMenuItem(this.setApplicationAction);
    private JMenuItem               setLocationMenuItem     = new JMenuItem(this.setLocationAction);
    private JCheckBoxMenuItem       generateKeyPairMenuItem = new JCheckBoxMenuItem(this.generateKeyPairAction);

    private JMenuItem               quitMenuItem            = new JMenuItem(this.quitAction);

    /*
     * Non-GUI members
     */
    public AcceptanceConsoleManager consoleManager          = AcceptanceConsoleManager.getInstance();


    /**
     * Main constructor.
     */
    public AcceptanceConsole() {

        super("Siemens Acceptance Test Console for OLAS WS Authentication");

        buildMenu();
        buildWindow();

        this.consoleManager.addObserver(this);
        AcceptanceConsoleManager.getInstance().addObserver(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(750, 600);
        this.setLocation(50, 50);
        setVisible(true);
    }

    private void buildMenu() {

        this.generateKeyPairMenuItem.setSelected(this.consoleManager.getGenerateKeyPair());

        this.authMenu.setMnemonic(KeyEvent.VK_A);

        this.authMenu.add(this.passwordMenuItem);
        this.authMenu.addSeparator();
        this.authMenu.add(this.setLocationMenuItem);
        this.authMenu.add(this.setApplicationMenuItem);
        this.authMenu.add(this.generateKeyPairMenuItem);
        this.authMenu.addSeparator();
        this.authMenu.add(this.quitMenuItem);

        JMenuBar menu = new JMenuBar();
        menu.add(this.authMenu);
        setJMenuBar(menu);
    }

    private void buildWindow() {

        buildStatusPanel();

        if (this.contentPanel == null) {
            this.contentPanel = new JPanel();
        }

        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.contentPanel, this.statusPanel);
        this.splitPane.setResizeWeight(1.0);
        this.splitPane.setDividerSize(3);

        this.add(this.splitPane);
    }

    private void buildStatusPanel() {

        setStatus();

        JPanel applicationPanel = new JPanel();
        applicationPanel.add(this.applicationLabel);

        JPanel locationPanel = new JPanel();
        locationPanel.add(this.locationLabel);

        this.statusPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, applicationPanel, locationPanel);
        this.statusPanel.setDividerSize(0);
        this.statusPanel.setResizeWeight(0.5);

    }

    public void resetContent() {

        this.contentPanel = new JPanel();
        this.splitPane.setTopComponent(this.contentPanel);
        resetAuthentication();
    }

    public void login(String deviceName, Object deviceCredentials) {

        this.contentPanel = new LoginPanel(this, "Authenticating ...");
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().authenticate(deviceName, deviceCredentials);

    }

    public void requestGlobalUsageAgreement() {

        this.contentPanel = new GlobalUsageAgreementPanel(this);
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().requestGlobalUsageAgreement();

    }

    public void confirmGlobalUsageAgreement(Confirmation confirmation) {

        this.contentPanel = new LoginPanel(this, "Confirming / Rejecting global usage agreement ...");
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().confirmGlobalUsageAgreement(confirmation);

    }

    public void requestUsageAgreement() {

        this.contentPanel = new UsageAgreementPanel(this);
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().requestUsageAgreement();

    }

    public void confirmUsageAgreement(Confirmation confirmation) {

        this.contentPanel = new LoginPanel(this, "Confirming / Rejecting usage agreement ...");
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().confirmUsageAgreement(confirmation);

    }

    public void getIdentity() {

        this.contentPanel = new IdentityConfirmationPanel(this);
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().getIdentity();

    }

    public void confirmIdentity(Confirmation confirmation) {

        this.contentPanel = new LoginPanel(this, "Confirming / Reject application's identity");
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().confirmIdentity(confirmation);

    }

    public void getMissingAttributes() {

        this.contentPanel = new MissingAttributesPanel(this);
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().getMissingAttributes();

    }

    public void saveMissingAttributes(List<Attribute> missingAttributes) {

        this.contentPanel = new LoginPanel(this, "Saving missing attributes");
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().saveMissingAttributes(missingAttributes);

    }

    protected void onAuthPassword() {

        resetAuthentication();
        this.contentPanel = new PasswordAuthentication(this);
        this.splitPane.setTopComponent(this.contentPanel);
    }

    private void resetAuthentication() {

        this.consoleManager.resetAuthenticationClient();
    }

    public void setStatus() {

        this.locationLabel.setText("Location: " + this.consoleManager.getLocation());
        this.applicationLabel.setText("Application: " + this.consoleManager.getApplication());
    }

    protected void onSetLocation() {

        String location = JOptionPane.showInputDialog("Set OLAS Authentication WS Location", this.consoleManager.getLocation());
        if (null != location) {
            this.consoleManager.setLocation(location);
        }
    }

    protected void onSetApplication() {

        String application = JOptionPane.showInputDialog("Set Application to authenticate against", this.consoleManager.getApplication());
        if (null != application) {
            this.consoleManager.setApplication(application);
        }
    }

    protected void onGenerateKeyPair() {

        this.consoleManager.setGenerateKeyPair(!this.consoleManager.getGenerateKeyPair());
        this.generateKeyPairMenuItem.setSelected(this.consoleManager.getGenerateKeyPair());

    }

    public void update(Observable o, Object arg) {

        if (o instanceof AcceptanceConsoleManager) {
            setStatus();
        }
    }


    /*
     * 
     * Action classes
     */
    public class AuthPasswordAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public AuthPasswordAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onAuthPassword();
        }
    }

    public class QuitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public QuitAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            LOG.info("Closing Swing Siemens Acceptance Test Console...");
            System.exit(0);
        }
    }

    public class SetLocationAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public SetLocationAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onSetLocation();
        }
    }

    public class SetApplicationAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public SetApplicationAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onSetApplication();
        }
    }

    public class GenerateKeyPairAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public GenerateKeyPairAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onGenerateKeyPair();
        }
    }

}
