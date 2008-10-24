/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.BROWSE;
import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.ERROR_MISSING_FIELDS;
import static net.link.safeonline.appconsole.Messages.ERROR_OPEN_KEYSTORE;
import static net.link.safeonline.appconsole.Messages.ERROR_SELECT_KEYSTORE;
import static net.link.safeonline.appconsole.Messages.KEYENTRY_PW;
import static net.link.safeonline.appconsole.Messages.KEYSTORE;
import static net.link.safeonline.appconsole.Messages.KEYSTORE_PW;
import static net.link.safeonline.appconsole.Messages.KEYSTORE_TYPE;
import static net.link.safeonline.appconsole.Messages.LOAD_IDENTITY;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.LineBorder;

import net.link.safeonline.sdk.KeyStoreUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Panel to load an application identify
 * 
 * @author wvdhaute
 * 
 */
public class LoadIdentity extends JPanel {

    private static final long  serialVersionUID      = 1L;

    private static final Log   LOG                   = LogFactory.getLog(LoadIdentity.class);

    private JComboBox          keyStoreTypeCombo     = new JComboBox();
    private JPasswordField     keyStorePasswordField = new JPasswordField(20);
    private JPasswordField     keyEntryPasswordField = new JPasswordField(20);
    private JLabel             keyStoreField         = new JLabel();

    /*
     * Actions
     */
    private Action             browseAction          = new BrowseAction(BROWSE.getMessage());
    private Action             loadAction            = new LoadAction(LOAD_IDENTITY.getMessage());
    private Action             cancelAction          = new CancelAction(CANCEL.getMessage());

    private ApplicationConsole parent                = null;


    /**
     * Main constructor.
     * 
     * @param applicationConsole
     */
    public LoadIdentity(ApplicationConsole applicationConsole) {

        super();
        this.parent = applicationConsole;
        init();
    }

    /*
     * Initialize the swing components
     */
    private void init() {

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        infoPanel.setLayout(gbl);

        JLabel keyStoreLabel = new JLabel(KEYSTORE.getMessage());
        JLabel keyStoreTypeLabel = new JLabel(KEYSTORE_TYPE.getMessage());
        JLabel keyStorePwLabel = new JLabel(KEYSTORE_PW.getMessage());
        JLabel keyEntryPasswordLabel = new JLabel(KEYENTRY_PW.getMessage());

        this.keyStoreTypeCombo.addItem("pkcs12");
        this.keyStoreTypeCombo.addItem("jks");

        this.keyStoreField.setBorder(new LineBorder(Color.black));

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

        JButton browseButton = new JButton(this.browseAction);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbl.setConstraints(browseButton, gbc);
        infoPanel.add(browseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbl.setConstraints(keyStoreTypeLabel, gbc);
        infoPanel.add(keyStoreTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(this.keyStoreTypeCombo, gbc);
        infoPanel.add(this.keyStoreTypeCombo, gbc);

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

        controlPanel.add(new JButton(this.loadAction));
        controlPanel.add(new JButton(this.cancelAction));

        this.setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    /*
     * Validate user input
     */
    protected boolean checkInput() {

        if (null == this.keyStoreField.getText() || this.keyStoreField.getText().length() == 0) {
            LOG.error("Please select a keystore...");
            JOptionPane.showMessageDialog(this, ERROR_SELECT_KEYSTORE.getMessage(), ERROR_MISSING_FIELDS.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    protected void onLoad() {

        if (!checkInput())
            return;
        String keyStorePath = this.keyStoreField.getText();
        String keyStoreType = (String) this.keyStoreTypeCombo.getSelectedItem();
        InputStream keyStoreInputStream;
        try {
            keyStoreInputStream = new FileInputStream(keyStorePath);
        } catch (FileNotFoundException e) {
            LOG.error("Failed to open file : " + keyStorePath, e);
            JOptionPane.showMessageDialog(this, ERROR_OPEN_KEYSTORE.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        char[] keyStorePassword = this.keyStorePasswordField.getPassword().length == 0? null: this.keyStorePasswordField.getPassword();
        char[] keyEntryPassword = this.keyEntryPasswordField.getPassword().length == 0? null: this.keyEntryPasswordField.getPassword();

        PrivateKeyEntry privateKeyEntry;
        try {
            privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(keyStoreType, keyStoreInputStream, keyStorePassword, keyEntryPassword);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.parent.resetContent();

        new ConfirmIdentity(privateKeyEntry, keyStorePath, keyStoreType, new String(keyStorePassword));
    }

    protected void onBrowse() {

        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new KeyStoreFilter());
        fc.showOpenDialog(this.getParent());
        File certFile = fc.getSelectedFile();
        if (certFile != null) {
            this.keyStoreField.setText(certFile.getAbsolutePath());
        }
    }

    protected void onCancel() {

        this.parent.resetContent();
    }


    /*
     * Filter for the KeyStore file chooser
     */
    class KeyStoreFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File file) {

            String filename = file.getName();
            if (file.isDirectory() || filename.endsWith(".p12") || filename.endsWith(".jks") || filename.endsWith(".pfx"))
                return true;
            return false;
        }

        @Override
        public String getDescription() {

            return "*.p12, *.pfx, *.jks";
        }
    }

    /*
     * Action classes
     */

    public class BrowseAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public BrowseAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onBrowse();
        }
    }

    public class CancelAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public CancelAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onCancel();
        }
    }

    public class LoadAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public LoadAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            onLoad();
        }
    }

}
