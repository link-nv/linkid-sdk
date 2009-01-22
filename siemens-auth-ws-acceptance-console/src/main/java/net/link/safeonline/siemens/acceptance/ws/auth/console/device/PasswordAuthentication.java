/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console.device;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.siemens.acceptance.ws.auth.console.AcceptanceConsole;
import net.link.safeonline.siemens.acceptance.ws.auth.console.DeviceAuthenticationPanel;


/**
 * Password Authentication Panel.
 * 
 * @author wvdhaute
 * 
 */
public class PasswordAuthentication extends DeviceAuthenticationPanel {

    private static final long   serialVersionUID                    = 1L;

    private static final String PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:password:ws:auth:login";
    private static final String PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE = "urn:net:lin-k:safe-online:password:ws:auth:password";

    private JTextField          loginField                          = new JTextField(20);
    private JPasswordField      passwordField                       = new JPasswordField(20);

    private JButton             loginButton                         = new JButton("Login");
    private JButton             cancelButton                        = new JButton("Cancel");


    public PasswordAuthentication(AcceptanceConsole parent) {

        super(parent);
        buildWindow();
        handleEvents();
    }

    public PasswordAuthentication(AcceptanceConsole parent, DeviceAuthenticationInformationType deviceAuthenticationInformation) {

        super(parent, deviceAuthenticationInformation);
        buildWindow();
        handleEvents();
    }

    private void buildWindow() {

        JPanel inputPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        inputPanel.setLayout(gbl);

        JLabel loginLabel = new JLabel("Login Name");
        JLabel passwordLabel = new JLabel("Password");

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = 1;
        gbc.insets = new Insets(5, 2, 5, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(loginLabel, gbc);
        inputPanel.add(loginLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(loginField, gbc);
        inputPanel.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbl.setConstraints(passwordLabel, gbc);
        inputPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(passwordField, gbc);
        inputPanel.add(passwordField, gbc);

        controlPanel.add(loginButton);
        controlPanel.add(cancelButton);

        setLayout(new BorderLayout());
        this.add(inputPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
    }

    private void handleEvents() {

        loginButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                if (!checkInput())
                    return;
                authenticate();
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

                cancel();
            }
        });
    }

    protected void authenticate() {

        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        Map<String, String> deviceCredentials = new HashMap<String, String>();
        deviceCredentials.put(PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, loginField.getText());
        deviceCredentials.put(PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, new String(passwordField.getPassword()));

        authenticate(deviceCredentials);

    }

    protected boolean checkInput() {

        if (null == loginField.getText() || loginField.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in the login name field", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in the password field", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
