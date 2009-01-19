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
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.auth.DataType;


/**
 * <h2>{@link IdentityConfirmationPanel}<br>
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
public class IdentityConfirmationPanel extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;

    AcceptanceConsole         parent           = null;

    private JPanel            infoPanel        = new JPanel();

    private JLabel            infoLabel        = new JLabel("Application Identity for application "
                                                       + AcceptanceConsoleManager.getInstance().getApplication());

    private JTable            identityTable    = null;

    private Action            confirmAction    = new ConfirmAction("Confirm");
    private JButton           confirmButton    = new JButton(this.confirmAction);

    private Action            rejectAction     = new RejectAction("Reject");
    private JButton           rejectButton     = new JButton(this.rejectAction);

    private Action            exitAction       = new ExitAction("Exit");
    private JButton           exitButton       = new JButton(this.exitAction);


    public IdentityConfirmationPanel(AcceptanceConsole parent) {

        this.parent = parent;

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        this.confirmButton.setEnabled(false);
        this.rejectButton.setEnabled(false);
        this.exitButton.setEnabled(false);

        JPanel controlPanel = new JPanel();

        this.infoPanel.setLayout(new BorderLayout());
        this.infoPanel.add(this.infoLabel, BorderLayout.NORTH);

        controlPanel.add(this.confirmButton);
        controlPanel.add(this.rejectButton);
        controlPanel.add(this.exitButton);

        setLayout(new BorderLayout());
        this.add(this.infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        this.exitButton.setEnabled(true);

        if (arg instanceof AuthenticationError) {
            AuthenticationError error = (AuthenticationError) arg;
            this.infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
        } else if (arg instanceof AuthenticationStep) {
            AuthenticationStep authenticationStep = (AuthenticationStep) arg;
            this.infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
        } else if (arg instanceof List<?>) {
            this.confirmButton.setEnabled(true);
            this.rejectButton.setEnabled(true);
            setIdentityTable((List<Attribute>) arg);
        }

    }

    private void setIdentityTable(List<Attribute> attributeList) {

        // calculate table size, beware compound members are nested
        int tableSize = attributeList.size();
        for (Attribute attribute : attributeList) {
            if (attribute.getDataType().equals(DataType.COMPOUNDED)) {
                tableSize += attribute.getMembers().size();
            }
        }

        // retrieve table data
        Object data[][] = new Object[tableSize][2];
        int idx = 0;
        for (Attribute attribute : attributeList) {
            data[idx][0] = attribute.getFriendlyName();
            data[idx][1] = attribute.isAnonymous();
            if (attribute.getDataType().equals(DataType.COMPOUNDED)) {
                for (Attribute memberAttribute : attribute.getMembers()) {
                    idx++;
                    data[idx][0] = memberAttribute.getFriendlyName();
                    data[idx][1] = memberAttribute.isAnonymous();
                }
            }
            idx++;
        }

        // set table
        this.identityTable = new JTable(new AttributesTableModel(attributeList, false));
        this.identityTable.getColumnModel().getColumn(1).setMaxWidth(75);
        JScrollPane tableScrollPane = new JScrollPane(this.identityTable);
        this.infoPanel.add(tableScrollPane, BorderLayout.CENTER);
        this.infoPanel.revalidate();
    }


    public class ExitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ExitAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            IdentityConfirmationPanel.this.parent.resetContent();
        }
    }

    public class ConfirmAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public ConfirmAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            IdentityConfirmationPanel.this.parent.confirmIdentity(Confirmation.CONFIRM);
        }
    }

    public class RejectAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public RejectAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            IdentityConfirmationPanel.this.parent.confirmIdentity(Confirmation.REJECT);
        }
    }

}
