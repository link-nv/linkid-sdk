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
import javax.swing.JTextArea;

import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;


/**
 * <h2>{@link UsageAgreementPanel}<br>
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
public class UsageAgreementPanel extends JPanel implements Observer {

    private static final long serialVersionUID   = 1L;

    AcceptanceConsole         parent             = null;

    private JLabel            infoLabel          = new JLabel("Usage Agreement for application "
                                                         + AcceptanceConsoleManager.getInstance().getApplication());

    private JTextArea         usageAgreementText = new JTextArea(10, 80);

    private Action            confirmAction      = new ConfirmAction("Confirm");
    private JButton           confirmButton      = new JButton(this.confirmAction);

    private Action            rejectAction       = new RejectAction("Reject");
    private JButton           rejectButton       = new JButton(this.rejectAction);

    private Action            exitAction         = new ExitAction("Exit");
    private JButton           exitButton         = new JButton(this.exitAction);


    public UsageAgreementPanel(AcceptanceConsole parent) {

        this.parent = parent;

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        this.usageAgreementText.setEditable(false);

        this.confirmButton.setEnabled(false);
        this.rejectButton.setEnabled(false);
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
        gbl.setConstraints(this.usageAgreementText, gbc);
        infoPanel.add(this.usageAgreementText, gbc);

        controlPanel.add(this.confirmButton);
        controlPanel.add(this.rejectButton);
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

        if (arg instanceof AuthenticationError) {
            AuthenticationError error = (AuthenticationError) arg;
            this.infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
        } else if (arg instanceof AuthenticationStep) {
            AuthenticationStep authenticationStep = (AuthenticationStep) arg;
            this.infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
        } else if (arg instanceof String) {
            this.confirmButton.setEnabled(true);
            this.rejectButton.setEnabled(true);
            this.usageAgreementText.setText((String) arg);
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

            UsageAgreementPanel.this.parent.resetContent();
        }
    }

    public class ConfirmAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ConfirmAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            UsageAgreementPanel.this.parent.confirmUsageAgreement(Confirmation.CONFIRM);
        }
    }

    public class RejectAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public RejectAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            UsageAgreementPanel.this.parent.confirmUsageAgreement(Confirmation.REJECT);
        }
    }

}
