/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.APPLICATION;
import static net.link.safeonline.appconsole.Messages.AUTH_USER;
import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.PROTOCOL;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.filter.LogManager;

import org.jdesktop.swingworker.SwingWorker;


/**
 * Panel to start the jetty authentication servlet
 * 
 * @author wvdhaute
 * 
 */
public class AuthUser extends JPanel implements Observer {

    private static final long serialVersionUID    = 1L;

    ApplicationConsole        parent              = null;
    String                    selectedProtocol    = null;

    private boolean           firstRequest        = true;

    /*
     * Actions
     */
    private Action            authAction          = new AuthAction(AUTH_USER.getMessage());
    private Action            cancelAction        = new CancelAction(CANCEL.getMessage());

    /*
     * View
     */
    private ButtonGroup       protocolButtonGroup = new ButtonGroup();
    private JRadioButton[]    protocolButtons     = null;
    JTextField                applicationField    = new JTextField(10);
    private JTextArea         requestArea1        = new JTextArea();
    private JTextArea         requestArea2        = new JTextArea();


    public AuthUser(ApplicationConsole applicationConsole) {

        this.parent = applicationConsole;
        setupProtocolButtons();
        buildWindow();

        // Listen to AuthServletManager to get notification if the
        // authentication ended
        AuthServletManager.getInstance().addObserver(this);
        // Listen to LogManager to get the log buffers via the LogFilter
        LogManager.getInstance().addObserver(this);
    }

    private void setupProtocolButtons() {

        AuthenticationProtocol[] protocols = AuthenticationProtocol.values();
        this.protocolButtons = new JRadioButton[protocols.length];
        for (int i = 0; i < protocols.length; i++) {
            this.protocolButtons[i] = new JRadioButton(protocols[i].toString());
            this.protocolButtonGroup.add(this.protocolButtons[i]);
        }
    }

    private void buildWindow() {

        JLabel applicationLabel = new JLabel(APPLICATION.getMessage());

        JSplitPane logPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(this.requestArea1), new JScrollPane(
                this.requestArea2));
        logPanel.setDividerSize(3);
        logPanel.setResizeWeight(0.5);
        this.requestArea1.setEnabled(false);
        this.requestArea2.setEnabled(false);
        this.requestArea1.setBorder(new TitledBorder("Request 1"));
        this.requestArea2.setBorder(new TitledBorder("Request 2"));

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
        gbl.setConstraints(this.applicationField, gbc);
        infoPanel.add(this.applicationField, gbc);

        int gridy = 1;
        for (JRadioButton protocolButton : this.protocolButtons) {
            JLabel protocolLabel = new JLabel(PROTOCOL.getMessage());

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
        controlPanel.add(new JButton(this.authAction));
        controlPanel.add(new JButton(this.cancelAction));

        JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, controlPanel);
        bottomPanel.setDividerSize(3);
        bottomPanel.setResizeWeight(1.0);

        JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logPanel, bottomPanel);
        splitPanel.setDividerSize(3);
        splitPanel.setResizeWeight(1.0);

        this.setLayout(new GridLayout());
        this.add(splitPanel);
    }

    public void update(Observable observable, Object object) {

        if (observable instanceof AuthServletManager) {
            if (object instanceof Boolean) {
                if ((Boolean) object) {
                    this.authAction.setEnabled(true);
                }
            }
        } else if (observable instanceof LogManager) {
            if (object instanceof StringBuffer) {
                if (this.firstRequest) {
                    this.firstRequest = false;
                    this.requestArea1.setText(((StringBuffer) object).toString());
                } else {
                    this.firstRequest = true;
                    this.requestArea2.setText(((StringBuffer) object).toString());
                }
            }
        }
    }

    public boolean checkInput() {

        String application = this.applicationField.getText().trim();
        if (null == application || application.equals(""))
            return false;

        boolean found = false;
        for (JRadioButton protocolButton : this.protocolButtons) {
            if (protocolButton.isSelected()) {
                this.selectedProtocol = protocolButton.getText();
                found = true;
            }
        }
        return found;
    }

    public void authenticate() {

        SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                AuthServletManager.getInstance().authenticate(AuthUser.this.applicationField.getText().trim(),
                        AuthUser.this.selectedProtocol);
                return Boolean.TRUE;
            }

        };
        worker.execute();
    }


    private class AuthAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public AuthAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, "Authenticate User");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            if (checkInput()) {
                setEnabled(false);
                authenticate();
            }
        }
    }

    private class CancelAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public CancelAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, "Cancel");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            AuthUser.this.parent.resetContent();
        }
    }

}
