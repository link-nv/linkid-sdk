/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console.device;

import java.awt.BorderLayout;
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
import javax.swing.JTextField;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.siemens.acceptance.ws.auth.console.AcceptanceConsole;
import net.link.safeonline.siemens.acceptance.ws.auth.console.DeviceAuthenticationPanel;


/**
 * Encap Authentication Panel.
 * 
 * @author wvdhaute
 * 
 */
public class EncapAuthentication extends DeviceAuthenticationPanel {

    private static final long   serialVersionUID               = 1L;

    private static final String ENCAP_WS_AUTH_MOBILE_ATTRIBUTE = "urn:net:lin-k:safe-online:encap:ws:auth:mobile";
    private static final String ENCAP_WS_AUTH_OTP_ATTRIBUTE    = "urn:net:lin-k:safe-online:encap:ws:auth:otp";

    private JTextField          mobileOrOtpField               = new JTextField(20);

    private JButton             loginButton                    = new JButton("Login");
    private JButton             cancelButton                   = new JButton("Cancel");


    public EncapAuthentication(AcceptanceConsole parent) {

        super(parent);
        buildWindow();
        handleEvents();
    }

    public EncapAuthentication(AcceptanceConsole parent, DeviceAuthenticationInformationType deviceAuthenticationInformation) {

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

        Map<String, String> deviceCredentials = new HashMap<String, String>();

        if (initial) {
            deviceCredentials.put(ENCAP_WS_AUTH_MOBILE_ATTRIBUTE, mobileOrOtpField.getText());
        } else {
            deviceCredentials.put(ENCAP_WS_AUTH_OTP_ATTRIBUTE, mobileOrOtpField.getText());
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

        return true;
    }
}
