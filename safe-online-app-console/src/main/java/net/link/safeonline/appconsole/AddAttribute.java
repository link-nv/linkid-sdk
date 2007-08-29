/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.ADD_ATTRIBUTE;
import static net.link.safeonline.appconsole.Messages.ATTRIBUTE_NAME;
import static net.link.safeonline.appconsole.Messages.ATTRIBUTE_TYPE;
import static net.link.safeonline.appconsole.Messages.ATTRIBUTE_VALUE;
import static net.link.safeonline.appconsole.Messages.CANCEL;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 * Add attribute window.
 * 
 * @author wvdhaute
 * 
 */
public class AddAttribute extends JFrame {

	private static final long serialVersionUID = 1L;

	private final String[] attributeTypes = { "String", "Boolean", "Date",
			"Integer", "Double" };

	private String user = null;

	private JTextField attributeNameField = new JTextField(15);
	private JComboBox attributeTypeCombo = new JComboBox();
	private JTextField attributeValueField = new JTextField(15);

	private Action addAttributeAction = new AddAttributeAction(ADD_ATTRIBUTE
			.getMessage());
	private Action cancelAction = new CancelAction(CANCEL.getMessage());

	public AddAttribute(String user) {
		super(ADD_ATTRIBUTE.getMessage());

		this.user = user;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		buildWindow();
		this.setLocation(50, 50);
		this.setSize(350, 200);
		this.setVisible(true);
	}

	private void buildWindow() {
		JPanel infoPanel = new JPanel();
		JPanel controlPanel = new JPanel();

		for (String attributeType : attributeTypes)
			attributeTypeCombo.addItem(attributeType);

		infoPanel.setLayout(new GridLayout(3, 2));
		infoPanel.add(new JLabel(ATTRIBUTE_NAME.getMessage()));
		infoPanel.add(attributeNameField);
		infoPanel.add(new JLabel(ATTRIBUTE_TYPE.getMessage()));
		infoPanel.add(attributeTypeCombo);
		infoPanel.add(new JLabel(ATTRIBUTE_VALUE.getMessage()));
		infoPanel.add(attributeValueField);

		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(new JButton(addAttributeAction));
		controlPanel.add(new JButton(cancelAction));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				infoPanel, controlPanel);
		splitPane.setDividerSize(3);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
	}

	private void closeFrame() {
		this.dispose();
	}

	private void addAttribute() {
		String attributeName = attributeNameField.getText();
		if (null == attributeName || attributeName.equals("")) {
			JOptionPane.showMessageDialog(this,
					"Please provide an attribute name", "",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String attributeType = (String) attributeTypeCombo.getSelectedItem();
	}

	private class AddAttributeAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public AddAttributeAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		}

		public void actionPerformed(ActionEvent evt) {
			addAttribute();
		}
	}

	private class CancelAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public CancelAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			closeFrame();
		}
	}

}
