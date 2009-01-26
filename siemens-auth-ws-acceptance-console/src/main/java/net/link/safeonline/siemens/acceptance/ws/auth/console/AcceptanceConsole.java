/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.siemens.acceptance.ws.auth.console.device.BeIdAuthentication;
import net.link.safeonline.siemens.acceptance.ws.auth.console.device.EncapAuthentication;
import net.link.safeonline.siemens.acceptance.ws.auth.console.device.OtpOverSmsAuthentication;
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

    private static final long                                                    serialVersionUID         = 1L;

    static final Log                                                             LOG                      = LogFactory
                                                                                                                      .getLog(AcceptanceConsole.class);

    public static final String                                                   PASSWORD_DEVICE_NAME     = "password";
    public static final String                                                   OTP_OVER_SMS_DEVICE_NAME = "OtpOverSms";
    public static final String                                                   ENCAP_DEVICE_NAME        = "encap";
    public static final String                                                   BEID_DEVICE_NAME         = "beid";

    private static final Map<String, Class<? extends DeviceAuthenticationPanel>> devicePanelMap           = new HashMap<String, Class<? extends DeviceAuthenticationPanel>>();

    static {
        registerDevicePanel(OTP_OVER_SMS_DEVICE_NAME, OtpOverSmsAuthentication.class);
        registerDevicePanel(PASSWORD_DEVICE_NAME, PasswordAuthentication.class);
        registerDevicePanel(ENCAP_DEVICE_NAME, EncapAuthentication.class);
        registerDevicePanel(BEID_DEVICE_NAME, BeIdAuthentication.class);
    }


    private static void registerDevicePanel(String deviceName, Class<? extends DeviceAuthenticationPanel> deviceAuthenticationPanelClass) {

        devicePanelMap.put(deviceName, deviceAuthenticationPanelClass);
    }


    /*
     * Actions
     */
    private Action                  setLocationAction       = new SetLocationAction("Set Location ...");
    private Action                  setApplicationAction    = new SetApplicationAction("Set Application ...");
    private Action                  generateKeyPairAction   = new GenerateKeyPairAction("Generate Keypair ...");
    private Action                  usePcscAppletAction     = new UsePcscAppletAction("Use pcsc applet ...");

    private Action                  quitAction              = new QuitAction("Quit");

    private Action                  acceptanceWsAction      = new AcceptanceWsAction("Acceptance Test WS ...");

    /*
     * GUI components
     */
    private JPanel                  contentPanel            = null;
    private JSplitPane              splitPane               = null;
    private JSplitPane              statusPanel             = null;

    private JLabel                  authenticatedLabel      = new JLabel();
    private JLabel                  locationLabel           = new JLabel();
    private JLabel                  applicationLabel        = new JLabel();

    /*
     * Menus
     */
    private JMenu                   setupMenu               = new JMenu("Setup");
    private JMenu                   authMenu                = new JMenu("OLAS WS Authentication");
    private JMenu                   wsMenu                  = new JMenu("Acceptance Test WS");

    private JMenuItem               setApplicationMenuItem  = new JMenuItem(setApplicationAction);
    private JMenuItem               setLocationMenuItem     = new JMenuItem(setLocationAction);
    private JCheckBoxMenuItem       generateKeyPairMenuItem = new JCheckBoxMenuItem(generateKeyPairAction);
    private JCheckBoxMenuItem       usePcscAppletMenuItem   = new JCheckBoxMenuItem(usePcscAppletAction);
    private JMenuItem               quitMenuItem            = new JMenuItem(quitAction);

    private JMenuItem               acceptanceWsMenuItem    = new JMenuItem(acceptanceWsAction);

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

        consoleManager.addObserver(this);
        AcceptanceConsoleManager.getInstance().addObserver(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(750, 600);
        this.setLocation(50, 50);
        setVisible(true);
    }

    private void buildMenu() {

        generateKeyPairMenuItem.setSelected(consoleManager.getGenerateKeyPair());
        usePcscAppletMenuItem.setSelected(consoleManager.getUsePcscApplet());
        acceptanceWsMenuItem.setEnabled(false);

        setupMenu.setMnemonic(KeyEvent.VK_S);
        authMenu.setMnemonic(KeyEvent.VK_A);

        setupMenu.add(setLocationMenuItem);
        setupMenu.add(setApplicationMenuItem);
        setupMenu.add(generateKeyPairMenuItem);
        setupMenu.addSeparator();
        setupMenu.add(usePcscAppletMenuItem);
        setupMenu.addSeparator();
        setupMenu.add(quitMenuItem);

        authMenu.add(new AuthenticationAction(PASSWORD_DEVICE_NAME));
        authMenu.add(new AuthenticationAction(OTP_OVER_SMS_DEVICE_NAME));
        authMenu.add(new AuthenticationAction(ENCAP_DEVICE_NAME));
        authMenu.add(new AuthenticationAction(BEID_DEVICE_NAME));

        wsMenu.add(acceptanceWsMenuItem);

        JMenuBar menu = new JMenuBar();
        menu.add(setupMenu);
        menu.add(authMenu);
        menu.add(wsMenu);
        setJMenuBar(menu);
    }

    private void buildWindow() {

        buildStatusPanel();

        if (contentPanel == null) {
            contentPanel = new JPanel();
        }

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPanel, statusPanel);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);

        this.add(splitPane);
    }

    private void buildStatusPanel() {

        setStatus();

        JPanel topPanel = new JPanel();
        topPanel.add(authenticatedLabel);

        JPanel applicationPanel = new JPanel();
        applicationPanel.add(applicationLabel);

        JPanel locationPanel = new JPanel();
        locationPanel.add(locationLabel);

        JSplitPane bottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, applicationPanel, locationPanel);
        bottomPanel.setDividerSize(0);
        bottomPanel.setResizeWeight(0.5);
        bottomPanel.setBorder(null);

        statusPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        statusPanel.setDividerSize(0);
        statusPanel.setResizeWeight(0.5);
        statusPanel.setBorder(null);
    }

    public void resetContent() {

        contentPanel = new JPanel();
        splitPane.setTopComponent(contentPanel);
        // resetAuthentication();
    }

    public void login(Object deviceCredentials) {

        contentPanel = new LoginPanel(this, "Authenticating ...");
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().authenticate(consoleManager.getDeviceName(), deviceCredentials);
    }

    public void requestGlobalUsageAgreement() {

        contentPanel = new GlobalUsageAgreementPanel(this);
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().requestGlobalUsageAgreement();

    }

    public void confirmGlobalUsageAgreement(Confirmation confirmation) {

        contentPanel = new LoginPanel(this, "Confirming / Rejecting global usage agreement ...");
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().confirmGlobalUsageAgreement(confirmation);

    }

    public void requestUsageAgreement() {

        contentPanel = new UsageAgreementPanel(this);
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().requestUsageAgreement();

    }

    public void confirmUsageAgreement(Confirmation confirmation) {

        contentPanel = new LoginPanel(this, "Confirming / Rejecting usage agreement ...");
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().confirmUsageAgreement(confirmation);

    }

    public void getIdentity() {

        contentPanel = new IdentityConfirmationPanel(this);
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().getIdentity();

    }

    public void confirmIdentity(Confirmation confirmation) {

        contentPanel = new LoginPanel(this, "Confirming / Reject application's identity");
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().confirmIdentity(confirmation);

    }

    public void getMissingAttributes() {

        contentPanel = new MissingAttributesPanel(this);
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().getMissingAttributes();

    }

    public void saveMissingAttributes(List<Attribute> missingAttributes) {

        contentPanel = new LoginPanel(this, "Saving missing attributes");
        splitPane.setTopComponent(contentPanel);

        AuthenticationUtils.getInstance().saveMissingAttributes(missingAttributes);

    }

    protected void onAuthenticate(String deviceName) {

        resetAuthentication();

        consoleManager.setDeviceName(deviceName);

        try {
            Class<? extends DeviceAuthenticationPanel> devicePanelClass = devicePanelMap.get(deviceName);
            Constructor<? extends DeviceAuthenticationPanel> constructor = devicePanelClass
                                                                                           .getConstructor(new Class[] { AcceptanceConsole.class });
            contentPanel = constructor.newInstance(this);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        splitPane.setTopComponent(contentPanel);
    }

    protected void onAuthenticateFurther(DeviceAuthenticationInformationType deviceAuthenticationInformation) {

        try {
            Class<? extends DeviceAuthenticationPanel> devicePanelClass = devicePanelMap.get(consoleManager.getDeviceName());
            Constructor<? extends DeviceAuthenticationPanel> constructor = devicePanelClass.getConstructor(new Class[] {
                    AcceptanceConsole.class, DeviceAuthenticationInformationType.class });
            contentPanel = constructor.newInstance(this, deviceAuthenticationInformation);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        splitPane.setTopComponent(contentPanel);
    }

    private void resetAuthentication() {

        consoleManager.resetAuthenticationClient();
    }

    public void setStatus() {

        if (null == consoleManager.getUserId()) {
            authenticatedLabel.setText("<html>OLAS Authentication: <font color=red>Not yet authenticated</font></html>");
        } else {
            authenticatedLabel.setText("<html>OLAS Authentication: user <i>" + consoleManager.getUserId() + "</i> using device <i>"
                    + consoleManager.getDeviceName() + "</i></html>");
            acceptanceWsMenuItem.setEnabled(true);
        }
        locationLabel.setText("<html>Location: <i>" + consoleManager.getLocation() + "</i></html>");
        applicationLabel.setText("<html>Application: <i>" + consoleManager.getApplication() + "</i></html>");
    }

    protected void onSetLocation() {

        String location = JOptionPane.showInputDialog("Set OLAS Authentication WS Location", consoleManager.getLocation());
        if (null != location) {
            consoleManager.setLocation(location);
        }
    }

    protected void onSetApplication() {

        String application = JOptionPane.showInputDialog("Set Application to authenticate against", consoleManager.getApplication());
        if (null != application) {
            consoleManager.setApplication(application);
        }
    }

    protected void onGenerateKeyPair() {

        consoleManager.setGenerateKeyPair(!consoleManager.getGenerateKeyPair());
        generateKeyPairMenuItem.setSelected(consoleManager.getGenerateKeyPair());

    }

    protected void onUsePcscApplet() {

        consoleManager.setUsePcscApplet(!consoleManager.getUsePcscApplet());
        usePcscAppletMenuItem.setSelected(consoleManager.getUsePcscApplet());

    }

    protected void onAcceptanceWS() {

        AuthenticationUtils.getInstance().acceptanceTestWs();

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
    public class AuthenticationAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private String            deviceName;


        public AuthenticationAction(String deviceName) {

            this.deviceName = deviceName;
            putValue(NAME, "Authenticate " + this.deviceName);
            putValue(SHORT_DESCRIPTION, "OLAS WS Authentication using device " + this.deviceName);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onAuthenticate(deviceName);
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

    public class UsePcscAppletAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public UsePcscAppletAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onUsePcscApplet();
        }
    }

    public class AcceptanceWsAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public AcceptanceWsAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onAcceptanceWS();
        }
    }

}
