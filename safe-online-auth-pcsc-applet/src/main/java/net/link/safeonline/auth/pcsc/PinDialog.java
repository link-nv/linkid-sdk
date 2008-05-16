/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PinDialog extends JDialog implements ActionListener,
		DocumentListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private JPasswordField passwordField;

	private JButton okButton;

	private JButton cancelButton;

	public PinDialog() {
		super((Frame) null, "PIN", true);
		JLabel promptLabel = new JLabel("PIN:");

		this.passwordField = new JPasswordField(4);
		this.passwordField.setEchoChar('*');
		this.passwordField.getDocument().addDocumentListener(this);
		this.passwordField.addKeyListener(this);

		JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		passwordPanel.add(this.passwordField);
		passwordPanel.add(Box.createHorizontalGlue());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.okButton = new JButton("OK");
		this.okButton.addActionListener(this);
		this.okButton.setEnabled(false);
		buttonPanel.add(this.okButton);
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		buttonPanel.add(this.cancelButton);

		JPanel contentPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Insets getInsets() {
				return new Insets(5, 20, 5, 20);
			}
		};
		contentPanel.setLayout(new GridLayout(3, 1));
		contentPanel.add(new JLabel("Geef uw PIN in, om u te authentiseren."));
		contentPanel.add(passwordPanel);
		contentPanel.add(buttonPanel);

		getContentPane().add(contentPanel);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				PinDialog.this.passwordField.requestFocusInWindow();
			}
		});

	}

	public String getPin() {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		if (false == this.okClicked) {
			return null;
		}
		String pin = new String(this.passwordField.getPassword());
		return pin;
	}

	private boolean okClicked;

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (this.okButton == source) {
			this.okClicked = true;
			dispose();
		} else if (this.cancelButton == source) {
			dispose();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		this.okButton.setEnabled(this.passwordField.getPassword().length == 4);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.okButton.setEnabled(this.passwordField.getPassword().length == 4);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (this.passwordField.getPassword().length == 4) {
				this.okClicked = true;
				dispose();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
