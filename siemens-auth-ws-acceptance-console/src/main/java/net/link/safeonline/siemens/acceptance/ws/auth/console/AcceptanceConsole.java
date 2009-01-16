/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.link.safeonline.sdk.ws.MessageAccessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;


/**
 * Siemens Acceptance Console main frame.
 * 
 * @author wvdhaute
 * 
 */
public class AcceptanceConsole extends JFrame implements Observer {

    private static final long       serialVersionUID     = 1L;

    static final Log                LOG                  = LogFactory.getLog(AcceptanceConsole.class);

    /*
     * Actions
     */
    private Action                  authPasswordAction   = new AuthPasswordAction("Password");

    private Action                  setLocationAction    = new SetLocationAction("Set Location");
    private Action                  setApplicationAction = new SetApplicationAction("Set Application");

    private Action                  captureAction        = new CaptureAction("Capture WS Messages");
    Action                          viewInboundAction    = new ViewInboundAction("View Inbound Message");
    Action                          viewOutboundAction   = new ViewOutboundAction("View Outbound Message");

    private Action                  quitAction           = new QuitAction("Quit");

    /*
     * GUI components
     */
    private JPanel                  contentPanel         = null;
    private JPanel                  debugPanel           = null;
    private JSplitPane              splitPane            = null;
    private JSplitPane              statusPanel          = null;

    private JLabel                  locationLabel        = new JLabel();

    /*
     * Menus
     */
    private JMenu                   authMenu             = new JMenu("OLAS WS Authentication");

    private JMenuItem               passwordMenuItem     = new JMenuItem(this.authPasswordAction);

    private JMenuItem               setLocationMenuItem  = new JMenuItem(this.setLocationAction);

    private JMenuItem               quitMenuItem         = new JMenuItem(this.quitAction);

    /*
     * Non-GUI members
     */
    public AcceptanceConsoleManager consoleManager       = AcceptanceConsoleManager.getInstance();


    /**
     * Main constructor.
     */
    public AcceptanceConsole() {

        super("Siemens Acceptance Test Console for OLAS WS Authentication");

        buildMenu();
        buildWindow();

        this.viewInboundAction.setEnabled(false);
        this.viewOutboundAction.setEnabled(false);
        this.consoleManager.addObserver(this);
        AcceptanceConsoleManager.getInstance().addObserver(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(750, 600);
        this.setLocation(50, 50);
        setVisible(true);
    }

    private void buildMenu() {

        this.authMenu.setMnemonic(KeyEvent.VK_A);

        this.authMenu.add(this.passwordMenuItem);
        this.authMenu.addSeparator();
        this.authMenu.add(this.setLocationMenuItem);
        this.authMenu.add(this.setApplicationAction);
        this.authMenu.addSeparator();
        this.authMenu.add(this.quitMenuItem);

        JMenuBar menu = new JMenuBar();
        menu.add(this.authMenu);
        setJMenuBar(menu);
    }

    private void buildWindow() {

        buildStatusPanel();
        buildDebugPanel();

        if (this.contentPanel == null) {
            this.contentPanel = new JPanel();
        }
        JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.debugPanel, this.statusPanel);
        bottomPanel.setDividerSize(3);
        bottomPanel.setResizeWeight(0.5);

        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.contentPanel, bottomPanel);
        this.splitPane.setResizeWeight(1.0);
        this.splitPane.setDividerSize(3);

        this.add(this.splitPane);
    }

    private void buildDebugPanel() {

        this.debugPanel = new JPanel(new FlowLayout());

        JCheckBox captureBox = new JCheckBox(this.captureAction);
        captureBox.setSelected(true);

        this.debugPanel.add(captureBox);
        this.debugPanel.add(new JButton(this.viewInboundAction));
        this.debugPanel.add(new JButton(this.viewOutboundAction));
    }

    private void buildStatusPanel() {

        this.statusPanel.add(this.locationLabel);
    }

    public void resetContent() {

        this.contentPanel = new JPanel();
        this.splitPane.setTopComponent(this.contentPanel);
        resetAuthentication();
    }

    public void login(String deviceName, Object deviceCredentials) {

        // this.contentPanel = new LoginPanel();
        this.splitPane.setTopComponent(this.contentPanel);

        AuthenticationUtils.getInstance().authenticate(deviceName, deviceCredentials);

    }

    protected void onAuthPassword() {

        resetAuthentication();
        this.contentPanel = new PasswordAuthentication(this);
    }

    private void resetAuthentication() {

        this.consoleManager.resetAuthenticationClient();
    }

    public void setStatus() {

        this.locationLabel.setText("Location: " + this.consoleManager.getLocation());
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

    public void update(Observable o, Object arg) {

        if (o instanceof AcceptanceConsoleManager) {
            if (arg instanceof MessageAccessor) {
                this.viewInboundAction.setEnabled(true);
                this.viewOutboundAction.setEnabled(true);
            } else {
                setStatus();
            }
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

    public class ViewInboundAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ViewInboundAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            Document inboundSoap = AcceptanceConsoleManager.getInstance().getInboundMessage();
            // TODO: show popup
            // launchBrowser(inboundSoap, "inbound_soap");
        }
    }

    public class ViewOutboundAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ViewOutboundAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            Document outboundSoap = AcceptanceConsoleManager.getInstance().getOutboundMessage();
            // TODO: show popup
            // launchBrowser(outboundSoap, "outbound_soap");
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
                AcceptanceConsoleManager.getInstance().setCaptureMessages(value.isSelected());
                AcceptanceConsole.this.viewInboundAction.setEnabled(value.isSelected());
                AcceptanceConsole.this.viewOutboundAction.setEnabled(value.isSelected());
            }
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

}
