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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

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
                                                         + AcceptanceConsoleManager.getInstance().getApplication(), SwingConstants.CENTER);

    private JTextArea         usageAgreementText = new JTextArea(10, 80);

    private Action            confirmAction      = new ConfirmAction("Confirm");
    private JButton           confirmButton      = new JButton(confirmAction);

    private Action            rejectAction       = new RejectAction("Reject");
    private JButton           rejectButton       = new JButton(rejectAction);

    private Action            exitAction         = new ExitAction("Exit");
    private JButton           exitButton         = new JButton(exitAction);


    public UsageAgreementPanel(AcceptanceConsole parent) {

        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        this.parent = parent;

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        usageAgreementText.setEditable(false);

        confirmButton.setEnabled(false);
        rejectButton.setEnabled(false);
        exitButton.setEnabled(false);

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(usageAgreementText, BorderLayout.CENTER);

        controlPanel.add(confirmButton);
        controlPanel.add(rejectButton);
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

        if (arg instanceof AuthenticationError) {
            AuthenticationError error = (AuthenticationError) arg;
            infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
        } else if (arg instanceof AuthenticationStep) {
            AuthenticationStep authenticationStep = (AuthenticationStep) arg;
            infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
        } else if (arg instanceof String) {
            confirmButton.setEnabled(true);
            rejectButton.setEnabled(true);
            usageAgreementText.setText((String) arg);
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

            parent.resetContent();
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

            parent.confirmUsageAgreement(Confirmation.CONFIRM);
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

            parent.confirmUsageAgreement(Confirmation.REJECT);
        }
    }

}
