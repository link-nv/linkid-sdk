/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.CERT_DN;
import static net.link.safeonline.appconsole.Messages.CREATE_P12;
import static net.link.safeonline.appconsole.Messages.ERROR_CREATE_P12;
import static net.link.safeonline.appconsole.Messages.ERROR_MISSING_FIELDS;
import static net.link.safeonline.appconsole.Messages.KEYENTRY_PW;
import static net.link.safeonline.appconsole.Messages.KEYSTORE;
import static net.link.safeonline.appconsole.Messages.KEYSTORE_PW;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.link.safeonline.sdk.KeyStoreUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Panel for creating p12 keystores.
 *
 * @author wvdhaute
 *
 */
public class CreateP12 extends JPanel {

    private static final long  serialVersionUID      = 1L;

    private static final Log   LOG                   = LogFactory.getLog(CreateP12.class);

    private JTextField         keyStoreField         = new JTextField(20);
    private JComboBox          keyStoreExt           = new JComboBox();
    private JTextField         nameField             = new JTextField(20);
    private JPasswordField     keyStorePasswordField = new JPasswordField(20);
    private JPasswordField     keyEntryPasswordField = new JPasswordField(20);

    private JButton            createButton          = new JButton(CREATE_P12.getMessage());
    private JButton            cancelButton          = new JButton(CANCEL.getMessage());

    private ApplicationConsole parent                = null;


    public CreateP12(ApplicationConsole applicationConsole) {

        this.parent = applicationConsole;
        buildWindow();
        handleEvents();
    }

    private void buildWindow() {

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        infoPanel.setLayout(gbl);

        JLabel keyStoreLabel = new JLabel(KEYSTORE.getMessage());
        this.keyStoreExt.addItem(".p12");
        this.keyStoreExt.addItem(".pfx");
        this.keyStoreExt.setSelectedIndex(0);
        JLabel keyStorePwLabel = new JLabel(KEYSTORE_PW.getMessage());
        JLabel keyEntryPasswordLabel = new JLabel(KEYENTRY_PW.getMessage());
        JLabel nameLabel = new JLabel(CERT_DN.getMessage());

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = 1;
        gbc.insets = new Insets(5, 2, 5, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(keyStoreLabel, gbc);
        infoPanel.add(keyStoreLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(this.keyStoreField, gbc);
        infoPanel.add(this.keyStoreField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbl.setConstraints(this.keyStoreExt, gbc);
        infoPanel.add(this.keyStoreExt, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbl.setConstraints(nameLabel, gbc);
        infoPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(this.nameField, gbc);
        infoPanel.add(this.nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbl.setConstraints(keyStorePwLabel, gbc);
        infoPanel.add(keyStorePwLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbl.setConstraints(this.keyStorePasswordField, gbc);
        infoPanel.add(this.keyStorePasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbl.setConstraints(keyEntryPasswordLabel, gbc);
        infoPanel.add(keyEntryPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbl.setConstraints(this.keyEntryPasswordField, gbc);
        infoPanel.add(this.keyEntryPasswordField, gbc);

        controlPanel.add(this.createButton);
        controlPanel.add(this.cancelButton);

        this.setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    private void handleEvents() {

        this.createButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                if (!checkInput())
                    return;
                onCreate();
            }
        });

        this.cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                onCancel();
            }
        });
    }

    protected boolean checkInput() {

        if (null == this.keyStoreField.getText() || this.keyStoreField.getText().length() == 0) {
            LOG.error("Please provide a keystore name...");
            JOptionPane.showMessageDialog(this, ERROR_MISSING_FIELDS.getMessage(), ERROR_MISSING_FIELDS.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (null == this.nameField.getText() || this.nameField.getText().length() == 0) {
            LOG.error("Please provide a distinguished name...");
            JOptionPane.showMessageDialog(this, ERROR_MISSING_FIELDS.getMessage(), ERROR_MISSING_FIELDS.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    protected void onCreate() {

        try {
            // get information
            String keyStoreName = this.keyStoreField.getText().trim();
            String certDN = this.nameField.getText().trim();
            char[] keyStorePassword = this.keyStorePasswordField.getPassword();
            char[] keyEntryPassword = this.keyEntryPasswordField.getPassword();

            // generate keypair
            KeyPair keyPair = KeyStoreUtils.generateKeyPair();

            // generate X509 certificate
            X509Certificate certificate = KeyStoreUtils.generateSelfSignedCertificate(keyPair, "CN=" + certDN);

            // persist P12 to keystore in /tmp
            File pkcs12keyStore = File.createTempFile(keyStoreName, (String) this.keyStoreExt.getSelectedItem());
            KeyStoreUtils.persistKey(pkcs12keyStore, keyPair.getPrivate(), certificate, keyStorePassword,
                    keyEntryPassword);

            // load generated identity
            InputStream keyStoreInputStream = new FileInputStream(pkcs12keyStore);
            PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry("pkcs12", keyStoreInputStream,
                    keyStorePassword, keyEntryPassword);

            String msg = "Successfully created P12 " + "\"" + pkcs12keyStore.getAbsolutePath() + "\"";
            LOG.info(msg);
            JOptionPane.showMessageDialog(this, msg);

            this.parent.consoleManager.setIdentity(privateKeyEntry, "", "", "");
            this.parent.resetContent();

        } catch (Exception ex) {
            LOG.error("Failed to generate a P12", ex);
            JOptionPane.showMessageDialog(this, ERROR_CREATE_P12.getMessage(), ERROR_CREATE_P12.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void onCancel() {

        this.parent.resetContent();
    }
}
