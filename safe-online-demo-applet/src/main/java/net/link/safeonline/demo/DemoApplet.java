/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class DemoApplet extends JApplet {

    private static final long serialVersionUID = 1L;


    public DemoApplet() {

        JTextArea outputArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane);

        outputArea.append("Hello World\n");

        String sessionId = this.getParameter("sessionid");
        outputArea.append("session id: " + sessionId + "\n");

    }
}
