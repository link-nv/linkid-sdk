/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.CERTIFICATE;
import static net.link.safeonline.appconsole.Messages.CERT_ISSUER_DN;
import static net.link.safeonline.appconsole.Messages.CERT_SIG_ALGO;
import static net.link.safeonline.appconsole.Messages.CERT_SUBJECT_DN;
import static net.link.safeonline.appconsole.Messages.CONFIRM_ID;
import static net.link.safeonline.appconsole.Messages.KEY_ALGO;
import static net.link.safeonline.appconsole.Messages.KEY_FORMAT;
import static net.link.safeonline.appconsole.Messages.PRIVATE_KEY;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * Confirm the application's certificate frame.
 *
 * @author wvdhaute
 *
 */
public class ConfirmIdentity extends JFrame {

    private static final long         serialVersionUID = 1L;

    private PrivateKeyEntry           privateKeyEntry  = null;
    private String                    keyStorePath;
    private String                    keyStoreType;
    private String                    keyStorePassword;

    private ApplicationConsoleManager consoleManager   = ApplicationConsoleManager.getInstance();

    private Action                    confirmAction    = new ConfirmAction(CONFIRM_ID.getMessage());
    private Action                    cancelAction     = new CancelAction(CANCEL.getMessage());


    public ConfirmIdentity(PrivateKeyEntry privateKeyEntry, String keyStorePath, String keyStoreType,
            String keyStorePassword) {

        super(CONFIRM_ID.getMessage());

        this.privateKeyEntry = privateKeyEntry;
        this.keyStorePath = keyStorePath;
        this.keyStoreType = keyStoreType;
        this.keyStorePassword = keyStorePassword;

        buildWindow();
        this.setVisible(true);
        this.setLocation(50, 50);
        this.setSize(400, 300);
    }

    private void buildWindow() {

        JPanel certificatePanel = new JPanel();
        JPanel privateKeyPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        /*
         * Certificate panel
         */
        JTextField issuerDNField = new JTextField(25);
        JTextField subjectDNField = new JTextField(25);
        JTextField sigAlgoField = new JTextField(25);
        issuerDNField.setEditable(false);
        subjectDNField.setEditable(false);
        sigAlgoField.setEditable(false);
        certificatePanel.setLayout(new GridLayout(3, 2));
        certificatePanel.add(new JLabel(CERT_ISSUER_DN.getMessage()));
        certificatePanel.add(issuerDNField);
        certificatePanel.add(new JLabel(CERT_SUBJECT_DN.getMessage()));
        certificatePanel.add(subjectDNField);
        certificatePanel.add(new JLabel(CERT_SIG_ALGO.getMessage()));
        certificatePanel.add(sigAlgoField);
        certificatePanel.setBorder(new TitledBorder(CERTIFICATE.getMessage()));

        issuerDNField.setText(((X509Certificate) this.privateKeyEntry.getCertificate()).getIssuerX500Principal()
                .getName());
        subjectDNField.setText(((X509Certificate) this.privateKeyEntry.getCertificate()).getSubjectX500Principal()
                .getName());
        sigAlgoField.setText(((X509Certificate) this.privateKeyEntry.getCertificate()).getSigAlgName());

        /*
         * Private key panel
         */
        JTextField keyAlgoField = new JTextField(25);
        JTextField keyFormatField = new JTextField(25);
        keyAlgoField.setEditable(false);
        keyFormatField.setEditable(false);
        privateKeyPanel.setLayout(new GridLayout(2, 2));
        privateKeyPanel.add(new JLabel(KEY_ALGO.getMessage()));
        privateKeyPanel.add(keyAlgoField);
        privateKeyPanel.add(new JLabel(KEY_FORMAT.getMessage()));
        privateKeyPanel.add(keyFormatField);
        privateKeyPanel.setBorder(new TitledBorder(PRIVATE_KEY.getMessage()));

        keyAlgoField.setText(this.privateKeyEntry.getPrivateKey().getAlgorithm());
        keyFormatField.setText(this.privateKeyEntry.getPrivateKey().getFormat());

        /*
         * Control panel
         */
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JButton(this.confirmAction));
        controlPanel.add(new JButton(this.cancelAction));

        /*
         * Add all to the parent container
         */
        JSplitPane contentPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, certificatePanel, privateKeyPanel);
        contentPanel.setDividerSize(3);
        contentPanel.setResizeWeight(0.5);

        JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, contentPanel, controlPanel);
        splitPanel.setDividerSize(3);
        splitPanel.setResizeWeight(1.0);

        this.add(splitPanel);
    }

    void onConfirm() {

        this.consoleManager.setIdentity(this.privateKeyEntry, this.keyStorePath, this.keyStoreType,
                this.keyStorePassword);
        this.dispose();
    }

    void onCancel() {

        this.consoleManager.setIdentity(null, null, null, null);
        this.dispose();
    }


    /*
     * Action classes
     */

    private class ConfirmAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ConfirmAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onConfirm();
        }
    }

    private class CancelAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public CancelAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onCancel();
        }
    }
}
