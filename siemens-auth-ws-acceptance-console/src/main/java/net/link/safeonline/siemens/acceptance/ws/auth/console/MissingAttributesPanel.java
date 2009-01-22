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
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.auth.DataType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link MissingAttributesPanel}<br>
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
public class MissingAttributesPanel extends JPanel implements Observer {

    static final Log          LOG                    = LogFactory.getLog(MissingAttributesPanel.class);

    private static final long serialVersionUID       = 1L;

    AcceptanceConsole         parent                 = null;

    private JPanel            infoPanel              = new JPanel();

    private JLabel            infoLabel              = new JLabel("Missing Attributes for application "
                                                             + AcceptanceConsoleManager.getInstance().getApplication());

    JTable                    missingAttributesTable = null;

    private Action            saveAction             = new SaveAction("Save");
    private JButton           saveButton             = new JButton(saveAction);

    private Action            cancelAction           = new CancelAction("Cancel");
    private JButton           cancelButton           = new JButton(cancelAction);


    public MissingAttributesPanel(AcceptanceConsole parent) {

        this.parent = parent;

        AuthenticationUtils.getInstance().addObserver(this);
        buildWindow();
    }

    /**
     * Initialize panel
     */
    private void buildWindow() {

        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        JPanel controlPanel = new JPanel();

        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(infoLabel, BorderLayout.NORTH);

        controlPanel.add(saveButton);
        controlPanel.add(cancelButton);

        setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        cancelButton.setEnabled(true);

        if (arg instanceof AuthenticationError) {
            AuthenticationError error = (AuthenticationError) arg;
            infoLabel.setText("Authentication failed: " + error.getCode().getErrorCode() + " message=" + error.getMessage());
        } else if (arg instanceof AuthenticationStep) {
            AuthenticationStep authenticationStep = (AuthenticationStep) arg;
            infoLabel.setText("Additional authentication step: " + authenticationStep.getValue());
        } else if (arg instanceof List<?>) {
            saveButton.setEnabled(true);
            setMissingAttributes((List<Attribute>) arg);
        }

    }

    private void setMissingAttributes(List<Attribute> attributeList) {

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
        missingAttributesTable = new JTable(new AttributesTableModel(attributeList, true));
        missingAttributesTable.getColumnModel().getColumn(1).setMaxWidth(75);
        missingAttributesTable.getColumnModel().getColumn(2).setMaxWidth(75);
        JScrollPane tableScrollPane = new JScrollPane(missingAttributesTable);
        infoPanel.add(tableScrollPane, BorderLayout.CENTER);
        infoPanel.revalidate();
    }


    public class CancelAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public CancelAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            parent.resetContent();
        }
    }

    public class SaveAction extends AbstractAction {

        private static final long serialVersionUID = 1L;


        public SaveAction(String name) {

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent evt) {

            // TODO: why the f#$k do I need to log the value to workaround some issue that it being null else ??
            List<Attribute> missingAttributes = ((AttributesTableModel) missingAttributesTable.getModel()).getAttributes();
            for (Attribute attribute : missingAttributes) {
                LOG.debug("missing attribute: " + attribute.getName());
                if (attribute.isCompounded()) {
                    for (Attribute memberAttribute : attribute.getMembers()) {
                        LOG.debug("missing attribute: member : " + memberAttribute.getName() + " value=" + memberAttribute.getValue());
                    }
                }
            }

            parent.saveMissingAttributes(missingAttributes);
        }
    }
}
