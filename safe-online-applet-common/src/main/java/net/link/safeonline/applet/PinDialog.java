/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

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


public class PinDialog extends JDialog implements ActionListener, DocumentListener, KeyListener {

    private static final long serialVersionUID = 1L;

    protected JPasswordField  passwordField;

    private JButton           okButton;

    private JButton           cancelButton;


    public PinDialog(String message) {

        super((Frame) null, "PIN", true);
        JLabel promptLabel = new JLabel("PIN:");

        passwordField = new JPasswordField(4);
        passwordField.setEchoChar('*');
        passwordField.getDocument().addDocumentListener(this);
        passwordField.addKeyListener(this);

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.add(promptLabel);
        passwordPanel.add(Box.createHorizontalStrut(5));
        passwordPanel.add(passwordField);
        passwordPanel.add(Box.createHorizontalGlue());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setEnabled(false);
        buttonPanel.add(okButton);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JPanel contentPanel = new JPanel() {

            private static final long serialVersionUID = 1L;


            @Override
            public Insets getInsets() {

                return new Insets(5, 20, 5, 20);
            }
        };
        contentPanel.setLayout(new GridLayout(3, 1));
        contentPanel.add(new JLabel(message));
        contentPanel.add(passwordPanel);
        contentPanel.add(buttonPanel);

        getContentPane().add(contentPanel);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {

                passwordField.requestFocusInWindow();
            }
        });

    }

    public String getPin() {

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        if (false == okClicked)
            return null;
        String pin = new String(passwordField.getPassword());
        return pin;
    }


    private boolean okClicked;


    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();
        if (okButton == source) {
            okClicked = true;
            dispose();
        } else if (cancelButton == source) {
            dispose();
        }
    }

    public void changedUpdate(DocumentEvent e) {

    }

    public void insertUpdate(DocumentEvent e) {

        okButton.setEnabled(passwordField.getPassword().length == 4);
    }

    public void removeUpdate(DocumentEvent e) {

        okButton.setEnabled(passwordField.getPassword().length == 4);
    }

    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (passwordField.getPassword().length == 4) {
                okClicked = true;
                dispose();
            }
        }
    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {

    }
}
