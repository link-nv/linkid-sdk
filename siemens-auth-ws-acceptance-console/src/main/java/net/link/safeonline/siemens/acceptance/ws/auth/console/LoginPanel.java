/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.auth.ws.AuthenticationStep;


/**
 * <h2>{@link LoginPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 19, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class LoginPanel extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;

    AcceptanceConsole         parent           = null;

    private JLabel            infoLabel        = null;
    private JProgressBar      progressBar      = new JProgressBar();

    private Action            exitAction       = new ExitAction("Exit");
    private JButton           exitButton       = new JButton(this.exitAction);


    public LoginPanel(AcceptanceConsole parent, String message) {

        this.parent = parent;
        this.infoLabel = new JLabel(message);

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        this.progressBar.setIndeterminate(true);
        this.exitButton.setEnabled(false);

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        infoPanel.setLayout(gbl);

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = 1;
        gbc.insets = new Insets(5, 2, 5, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(this.infoLabel, gbc);
        infoPanel.add(this.infoLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbl.setConstraints(this.progressBar, gbc);
        infoPanel.add(this.progressBar, gbc);

        controlPanel.add(this.exitButton);

        setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);

    }

    /**
     * {@inheritDoc}
     */
    public void update(Observable o, Object arg) {

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.exitButton.setEnabled(true);
        this.progressBar.setVisible(false);

        if (arg instanceof AuthenticationError) {
            AuthenticationError error = (AuthenticationError) arg;
            this.infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
        } else if (arg instanceof AuthenticationStep) {
            AuthenticationStep authenticationStep = (AuthenticationStep) arg;
            if (authenticationStep.equals(AuthenticationStep.GLOBAL_USAGE_AGREEMENT)) {
                this.parent.requestGlobalUsageAgreement();
            } else if (authenticationStep.equals(AuthenticationStep.USAGE_AGREEMENT)) {
                this.parent.requestUsageAgreement();
            } else if (authenticationStep.equals(AuthenticationStep.IDENTITY_CONFIRMATION)) {
                this.parent.getIdentity();
            } else if (authenticationStep.equals(AuthenticationStep.MISSING_ATTRIBUTES)) {
                this.parent.getMissingAttributes();
            } else {
                this.infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
            }
        } else if (arg instanceof DeviceAuthenticationInformationType) {
            DeviceAuthenticationInformationType deviceAuthenticationInformation = (DeviceAuthenticationInformationType) arg;
            this.parent.onAuthenticateFurther(deviceAuthenticationInformation);
        } else if (arg instanceof String) {
            // success
            this.infoLabel.setText("Successfully authenticated user " + (String) arg);
        }

    }


    public class ExitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ExitAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            LoginPanel.this.parent.resetContent();
        }
    }

}
