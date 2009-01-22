/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.BorderLayout;
import java.awt.Cursor;
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
import javax.swing.SwingConstants;

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
    private JButton           exitButton       = new JButton(exitAction);


    public LoginPanel(AcceptanceConsole parent, String message) {

        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        this.parent = parent;
        infoLabel = new JLabel(message, SwingConstants.CENTER);

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        progressBar.setIndeterminate(true);
        exitButton.setEnabled(false);

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.add(progressBar, BorderLayout.SOUTH);

        controlPanel.add(exitButton);

        setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);

    }

    /**
     * {@inheritDoc}
     */
    public void update(Observable o, Object arg) {

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        exitButton.setEnabled(true);
        progressBar.setVisible(false);

        try {
            if (arg instanceof AuthenticationError) {
                AuthenticationError error = (AuthenticationError) arg;
                infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
            } else if (arg instanceof AuthenticationStep) {
                AuthenticationStep authenticationStep = (AuthenticationStep) arg;
                if (authenticationStep.equals(AuthenticationStep.GLOBAL_USAGE_AGREEMENT)) {
                    parent.requestGlobalUsageAgreement();
                } else if (authenticationStep.equals(AuthenticationStep.USAGE_AGREEMENT)) {
                    parent.requestUsageAgreement();
                } else if (authenticationStep.equals(AuthenticationStep.IDENTITY_CONFIRMATION)) {
                    parent.getIdentity();
                } else if (authenticationStep.equals(AuthenticationStep.MISSING_ATTRIBUTES)) {
                    parent.getMissingAttributes();
                } else {
                    infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
                }
            } else if (arg instanceof DeviceAuthenticationInformationType) {
                DeviceAuthenticationInformationType deviceAuthenticationInformation = (DeviceAuthenticationInformationType) arg;
                parent.onAuthenticateFurther(deviceAuthenticationInformation);
            } else if (arg instanceof String) {
                // success
                parent.consoleManager.setUserId((String) arg);
                parent.resetContent();
            }
        } finally {
            cleanup();
        }

    }

    protected void cleanup() {

        AuthenticationUtils.getInstance().deleteObserver(this);

    }


    public class ExitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ExitAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            parent.resetContent();
            cleanup();
        }
    }

}
