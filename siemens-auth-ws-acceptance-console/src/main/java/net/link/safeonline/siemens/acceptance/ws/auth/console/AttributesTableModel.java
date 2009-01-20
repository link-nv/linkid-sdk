/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.auth.DataType;


/**
 * <h2>{@link AttributesTableModel}<br>
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
public class AttributesTableModel extends DefaultTableModel {

    private static final long   serialVersionUID = 1L;

    private static final String DATE_FORMAT      = "dd MM yyyy";

    private SimpleDateFormat    dateFormat       = null;

    private final String[]      columnNames      = { "Name", "Anonymous", "Optional", "Value" };

    private List<Attribute>     attributes;

    private boolean             editable;

    private int                 rows             = 0;

    private RowMap[]            rowMapping;


    class RowMap {

        private int attributeIndex;
        private int memberIndex;


        public RowMap(int attributeIndex, int memberIndex) {

            this.attributeIndex = attributeIndex;
            this.memberIndex = memberIndex;
        }

        public int getAttributeIndex() {

            return attributeIndex;
        }

        public int getMemberIndex() {

            return memberIndex;
        }
    }


    public AttributesTableModel(List<Attribute> attributes, boolean editable) {

        dateFormat = new SimpleDateFormat(DATE_FORMAT);

        this.attributes = attributes;
        this.editable = editable;

        // calculate table size
        rows = attributes.size();
        for (Attribute attribute : attributes) {
            if (attribute.isCompounded()) {
                rows += attribute.getMembers().size();
            }
        }

        // create table mapping as compound members are nested
        rowMapping = new RowMap[rows];
        int mappingIdx = 0;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            rowMapping[mappingIdx++] = new RowMap(i, -1);
            if (attribute.isCompounded()) {
                for (int j = 0; j < attribute.getMembers().size(); j++) {
                    rowMapping[mappingIdx++] = new RowMap(i, j);
                }
            }
        }
    }

    public List<Attribute> getAttributes() {

        return attributes;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {

        Attribute attribute = getAttribute(row);

        try {
            if (attribute.getDataType().equals(DataType.BOOLEAN)) {
                attribute.setValue(Boolean.parseBoolean((String) value));
            } else if (attribute.getDataType().equals(DataType.DOUBLE)) {
                attribute.setValue(Double.parseDouble((String) value));
            } else if (attribute.getDataType().equals(DataType.INTEGER)) {
                attribute.setValue(Integer.parseInt((String) value));
            } else if (attribute.getDataType().equals(DataType.STRING)) {
                attribute.setValue(value);
            } else if (attribute.getDataType().equals(DataType.DATE)) {
                try {
                    attribute.setValue(dateFormat.parse((String) value));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(new JFrame(), "Invalid input, not a valid date ( format=\"" + DATE_FORMAT + "\" )",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Unsupported data type: " + attribute.getDataType().getValue(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }

        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int col) {

        if (0 == attributes.size())
            return null;

        Attribute attribute = getAttribute(row);

        switch (col) {
            case 0:
                return attribute.getFriendlyName();
            case 1:
                return attribute.isAnonymous();
            case 2:
                return attribute.isOptional();
            case 3: {
                if (null == attribute.getValue())
                    return null;
                if (attribute.getDataType().equals(DataType.DATE))
                    return dateFormat.format((Date) attribute.getValue());
                return attribute.getValue();
            }
            default:
                return null;
        }

    }

    private Attribute getAttribute(int row) {

        if (rowMapping[row].getMemberIndex() >= 0)
            return attributes.get(rowMapping[row].getAttributeIndex()).getMembers().get(rowMapping[row].getMemberIndex());

        return attributes.get(rowMapping[row].getAttributeIndex());

    }

    @Override
    public int getRowCount() {

        return rows;
    }

    @Override
    public boolean isCellEditable(int row, int column) {

        if (column != 3)
            return false;

        if (getAttribute(row).isCompounded())
            return false;
        return editable;
    }

    @Override
    public int getColumnCount() {

        if (editable)
            return columnNames.length;

        // not editable table => identity confirmation table => value column not shown
        return columnNames.length - 1;
    }

    @Override
    public String getColumnName(int col) {

        return columnNames[col];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getColumnClass(int c) {

        Object o = getValueAt(0, c);
        if (o == null)
            return Object.class;
        return o.getClass();
    }

}
