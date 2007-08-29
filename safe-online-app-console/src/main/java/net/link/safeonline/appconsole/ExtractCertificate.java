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
import static net.link.safeonline.appconsole.Messages.EXTRACT_CERT;
import static net.link.safeonline.appconsole.Messages.KEYENTRY_PW;
import static net.link.safeonline.appconsole.Messages.KEYSTORE;
import static net.link.safeonline.appconsole.Messages.KEYSTORE_PW;
import static net.link.safeonline.appconsole.Messages.KEYSTORE_TYPE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PrivateKeyEntry;

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
 * Panel to extract a certificate in DER format from a keystore.
 * 
 * @author wvdhaute
 * 
 */
public class ExtractCertificate extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ExtractCertificate.class);

	private JComboBox keyStoreTypeCombo = new JComboBox();;
	private JPasswordField keyStorePasswordField = new JPasswordField(20);
	private JPasswordField keyEntryPasswordField = new JPasswordField(20);
	private JLabel keyStoreField = new JLabel();

	private JButton browseButton = new JButton(BROWSE.getMessage());
	private JButton extractButton = new JButton(EXTRACT_CERT.getMessage());
	private JButton cancelButton = new JButton(CANCEL.getMessage());

	private ApplicationConsole parent = null;

	/**
	 * Main constructor.
	 */
	public ExtractCertificate(ApplicationConsole applicationConsole) {
		super();
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
		JLabel keyStoreTypeLabel = new JLabel(KEYSTORE_TYPE.getMessage());
		JLabel keyStorePwLabel = new JLabel(KEYSTORE_PW.getMessage());
		JLabel keyEntryPasswordLabel = new JLabel(KEYENTRY_PW.getMessage());

		keyStoreTypeCombo.addItem("pkcs12");
		keyStoreTypeCombo.addItem("jks");

		keyStoreField.setBorder(new LineBorder(Color.black));

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
		gbl.setConstraints(keyStoreField, gbc);
		infoPanel.add(keyStoreField, gbc);

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
		gbl.setConstraints(keyStoreTypeCombo, gbc);
		infoPanel.add(keyStoreTypeCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbl.setConstraints(keyStorePwLabel, gbc);
		infoPanel.add(keyStorePwLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbl.setConstraints(keyStorePasswordField, gbc);
		infoPanel.add(keyStorePasswordField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbl.setConstraints(keyEntryPasswordLabel, gbc);
		infoPanel.add(keyEntryPasswordLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 3;
		gbl.setConstraints(keyEntryPasswordField, gbc);
		infoPanel.add(keyEntryPasswordField, gbc);

		controlPanel.add(extractButton);
		controlPanel.add(cancelButton);

		this.setLayout(new BorderLayout());
		this.add(infoPanel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	private void handleEvents() {
		extractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!checkInput())
					return;
				else
					onExtract();
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onBrowse();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onCancel();
			}
		});
	}

	protected void onExtract() {
		String keyStorePath = keyStoreField.getText();
		String keyStoreType = (String) keyStoreTypeCombo.getSelectedItem();
		InputStream keyStoreInputStream;
		try {
			keyStoreInputStream = new FileInputStream(keyStorePath);
		} catch (FileNotFoundException e) {
			LOG.error("Failed to open file : " + keyStorePath, e);
			JOptionPane.showMessageDialog(this, ERROR_OPEN_KEYSTORE
					.getMessage(), "", JOptionPane.ERROR_MESSAGE);
			return;
		}
		char[] keyStorePassword = keyStorePasswordField.getPassword().length == 0 ? null
				: keyStorePasswordField.getPassword();
		char[] keyEntryPassword = keyEntryPasswordField.getPassword().length == 0 ? null
				: keyEntryPasswordField.getPassword();

		PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(
				keyStoreType, keyStoreInputStream, keyStorePassword,
				keyEntryPassword);

		File certificateFile;
		try {
			certificateFile = File.createTempFile("extracted_certificate",
					".crt");
			CertificateUtils.extractCertificate(privateKeyEntry,
					certificateFile);
			String msg = "Extraced certificate from keystore=\"" + keyStorePath + "\" to file: \"" + certificateFile.getAbsolutePath() + "\"";
			LOG.info(msg);
			JOptionPane.showMessageDialog(this, msg);
			this.parent.resetContent();
		} catch (IOException e) {
			LOG.error("Failed to extrace certificate : " + e.getMessage(), e);
			return;
		}
	}

	protected void onCancel() {
		this.parent.resetContent();
	}

	protected void onBrowse() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new KeyStoreFilter());
		fc.showOpenDialog(this.getParent());
		File certFile = fc.getSelectedFile();
		if (certFile != null)
			keyStoreField.setText(certFile.getAbsolutePath());
	}

	protected boolean checkInput() {
		if (null == keyStoreField.getText()
				|| keyStoreField.getText().length() == 0) {
			LOG.error("Please select a keystore...");
			JOptionPane.showMessageDialog(this, ERROR_SELECT_KEYSTORE
					.getMessage(), ERROR_MISSING_FIELDS.getMessage(),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	class KeyStoreFilter extends javax.swing.filechooser.FileFilter {
		public boolean accept(File file) {
			String filename = file.getName();
			if (file.isDirectory() || filename.endsWith(".pkcs12")
					|| filename.endsWith(".jks"))
				return true;
			else
				return false;
		}

		public String getDescription() {
			return "*.pkcs12, *.jks";
		}
	}
}
