/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;


/**
 * <h2>{@link AcceptanceWSPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 19, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AcceptanceWSPanel extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;

    AcceptanceConsole         parent           = null;

    private JLabel            infoLabel        = new JLabel("Acceptance Test WS", SwingConstants.CENTER);

    private JTextArea         attributeText    = new JTextArea(10, 80);

    private Action            exitAction       = new ExitAction("Exit");
    private JButton           exitButton       = new JButton(exitAction);


    public AcceptanceWSPanel(AcceptanceConsole parent) {

        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        this.parent = parent;

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        attributeText.setEditable(false);

        exitButton.setEnabled(false);

        JPanel infoPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(attributeText, BorderLayout.CENTER);

        controlPanel.add(exitButton);

        setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);

    }

    /**
     * {@inheritDoc}
     */
    public void update(Observable o, Object arg) {

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        exitButton.setEnabled(true);

        try {
            if (arg instanceof String) {
                attributeText.setText("Returned attribute: " + (String) arg);
            }
        } finally {
            cleanup();
        }

    }

    protected void cleanup() {

        AuthenticationUtils.getInstance().deleteObserver(this);

    }


    public class ExitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ExitAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            parent.resetContent();
            cleanup();
        }
    }
}
