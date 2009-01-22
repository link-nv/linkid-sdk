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
 * OTP over SMS Authentication Panel.
 * 
 * @author wvdhaute
 * 
 */
public class OtpOverSmsAuthentication extends DeviceAuthenticationPanel {

    private static final long   serialVersionUID                    = 1L;

    private static final String OTPOVERSMS_WS_AUTH_MOBILE_ATTRIBUTE = "urn:net:lin-k:safe-online:otpoversms:ws:auth:mobile";
    private static final String OTPOVERSMS_WS_AUTH_OTP_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:otp";
    private static final String OTPOVERSMS_WS_AUTH_PIN_ATTRIBUTE    = "urn:net:lin-k:safe-online:otpoversms:ws:auth:pin";

    private JTextField          mobileOrOtpField                    = new JTextField(20);
    private JPasswordField      pinField                            = new JPasswordField(20);

    private JButton             loginButton                         = new JButton("Login");
    private JButton             cancelButton                        = new JButton("Cancel");


    public OtpOverSmsAuthentication(AcceptanceConsole parent) {

        super(parent);
        buildWindow();
        handleEvents();
    }

    public OtpOverSmsAuthentication(AcceptanceConsole parent, DeviceAuthenticationInformationType deviceAuthenticationInformation) {

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

        JLabel mobileOrOtpLabel;
        if (initial) {
            mobileOrOtpLabel = new JLabel("Mobile");
        } else {
            mobileOrOtpLabel = new JLabel("OTP");
        }
        JLabel pinLabel = new JLabel("Pin");

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = 1;
        gbc.insets = new Insets(5, 2, 5, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(mobileOrOtpLabel, gbc);
        inputPanel.add(mobileOrOtpLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(mobileOrOtpField, gbc);
        inputPanel.add(mobileOrOtpField, gbc);

        if (!initial) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbl.setConstraints(pinLabel, gbc);
            inputPanel.add(pinLabel, gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbl.setConstraints(pinField, gbc);
            inputPanel.add(pinField, gbc);
        }

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

        if (initial) {
            deviceCredentials.put(OTPOVERSMS_WS_AUTH_MOBILE_ATTRIBUTE, mobileOrOtpField.getText());
        } else {
            deviceCredentials.put(OTPOVERSMS_WS_AUTH_OTP_ATTRIBUTE, mobileOrOtpField.getText());
            deviceCredentials.put(OTPOVERSMS_WS_AUTH_PIN_ATTRIBUTE, new String(pinField.getPassword()));
        }

        authenticate(deviceCredentials);

    }

    protected boolean checkInput() {

        if (null == mobileOrOtpField.getText() || mobileOrOtpField.getText().length() == 0) {
            if (initial) {
                JOptionPane.showMessageDialog(this, "Please fill in the mobile field", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in the otp field", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        if (!initial) {
            if (pinField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in the pin field", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
