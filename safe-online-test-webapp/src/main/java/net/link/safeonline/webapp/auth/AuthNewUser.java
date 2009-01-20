/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.auth;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import junit.framework.Assert;
import net.link.safeonline.webapp.AcceptanceTestManager;
import net.link.safeonline.webapp.Page;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;


public class AuthNewUser extends Page {

    public static final String  PAGE_NAME = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/new-user.seam";

    private static final String CAPTCHA   = SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/captcha.jpg";

    AcceptanceTestManager       acceptanceTestManager;

    String                      captcha;


    public AuthNewUser() {

        super(PAGE_NAME);
    }

    public void setLogin(String login) {

        fillInputField("login", login);
    }

    public void setCaptcha(AcceptanceTestManager acceptanceTestManager) {

        this.acceptanceTestManager = acceptanceTestManager;
        getCaptcha();
        fillInputField("captcha", captcha);
    }

    public AuthNewUserDevice register() {

        clickButtonAndWait("register");
        return new AuthNewUserDevice();
    }

    private void getCaptcha() {

        String jSessionId = getJSessionID();
        LOG.debug("session id: " + jSessionId);
        JFrame captchaFrame = new CaptchaFrame(jSessionId);
        while (captchaFrame.isShowing()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Assert.fail("Thread interrupted");
            }
        }
        if (null == captcha) {
            captcha = "";
        }
    }

    private String getJSessionID() {

        String cookies = Page.getSelenium().getCookie();
        if (cookies.indexOf("JSESSIONID") == -1)
            return null;
        StringTokenizer st = new StringTokenizer(cookies);
        while (st.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
            while (st2.hasMoreTokens()) {
                String key = st2.nextToken();
                String val = st2.nextToken();
                if (key.equals("JSESSIONID"))
                    return val;
            }
        }
        return null;
    }


    private class CaptchaFrame extends JFrame {

        private static final long serialVersionUID = 1L;

        private JLabel            label            = new JLabel();
        private JTextField        captchaText      = new JTextField(15);
        private JButton           refresh          = new JButton("Refresh");
        private JButton           submit           = new JButton("Submit");

        private String            jSessionId;


        public CaptchaFrame(String jSessionId) {

            this.jSessionId = jSessionId;
            loadCaptcha();

            JPanel imagePanel = new JPanel(new FlowLayout());
            imagePanel.add(label);
            imagePanel.add(refresh);

            JPanel inputPanel = new JPanel(new FlowLayout());
            inputPanel.add(captchaText);
            inputPanel.add(submit);

            getContentPane().add(imagePanel, BorderLayout.CENTER);
            getContentPane().add(inputPanel, BorderLayout.SOUTH);
            setTitle("Captcha");
            pack();
            setVisible(true);

            handleEvents();
        }

        private void handleEvents() {

            submit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    captcha = getCaptchaText();
                    close();
                }
            });

            refresh.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    loadCaptcha();
                }
            });
        }

        public void loadCaptcha() {

            try {
                HttpClient httpClient = new HttpClient();
                HttpMethod method = new GetMethod(acceptanceTestManager.getSafeOnlineLocation() + CAPTCHA);
                method.setRequestHeader("Cookie", "JSESSIONID=" + jSessionId);

                httpClient.executeMethod(method);
                Image captchaImage = ImageIO.read(method.getResponseBodyAsStream());
                label.setIcon(new ImageIcon(captchaImage));

            } catch (IOException e) {
                return;
            }
        }

        public String getCaptchaText() {

            return captchaText.getText();
        }

        public void close() {

            dispose();
        }

    }

}
