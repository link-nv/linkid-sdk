/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.ADD_ATTRIBUTE;
import static net.link.safeonline.appconsole.Messages.DELETE;
import static net.link.safeonline.appconsole.Messages.DONE;
import static net.link.safeonline.appconsole.Messages.GET_ATTRIBUTES;
import static net.link.safeonline.appconsole.Messages.USER;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.link.safeonline.sdk.ws.CompoundUtil;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Attribute panel, uses the Safe Online attribute and data webservices via the
 * ServicesUtils class
 * 
 * @author wvdhaute
 * 
 */
public class AttribService extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(AttribService.class);

	private ApplicationConsole parent = null;
	private TreePath treePath = null;

	/*
	 * Actions
	 */
	private Action getAttributesAction = new GetAttributesAction(GET_ATTRIBUTES
			.getMessage());
	private Action deleteAttributeAction = new DeleteAttributeAction(DELETE
			.getMessage());
	private Action addAttributeAction = new AddAttributeAction(ADD_ATTRIBUTE
			.getMessage());
	private Action doneAction = new DoneAction(DONE.getMessage());

	/*
	 * View
	 */
	private JLabel userLabel = new JLabel(USER.getMessage());
	private JTextField userField = new JTextField(20);

	private DefaultMutableTreeNode top = new DefaultMutableTreeNode(
			"User Attributes");
	private DefaultMutableTreeNode userNode = null;
	private JTree attributeTree = new JTree(top);
	private DefaultTreeModel attributeTreeModel = (DefaultTreeModel) attributeTree
			.getModel();

	// JTree popup for deleting attributes
	private JPopupMenu treePopup = new JPopupMenu();

	/*
	 * Constructor
	 */
	public AttribService(ApplicationConsole applicationConsole) {
		this.parent = applicationConsole;
		ServicesUtils.getInstance().addObserver(this);
		init();
		initMenu();
		registerListeners();
	}

	/*
	 * Initialize swing components
	 */
	private void init() {
		JPanel infoPanel = new JPanel();
		JScrollPane treePanel = new JScrollPane(attributeTree);
		attributeTree.setEditable(true);
		// DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)
		// attributeTree
		// .getCellRenderer();
		// TreeCellEditor editor = new AttributeCellEditor(attributeTree,
		// renderer);
		// attributeTree.setCellEditor(editor);
		attributeTreeModel
				.addTreeModelListener(new AttributeTreeModelListener());

		infoPanel.setLayout(new FlowLayout());
		infoPanel.add(userLabel);
		infoPanel.add(userField);
		JButton getAttributesButton = new JButton(getAttributesAction);
		getAttributesButton.setMultiClickThreshhold(1000); // prevent double
		infoPanel.add(getAttributesButton);
		infoPanel.add(new JButton(addAttributeAction));
		infoPanel.add(new JButton(doneAction));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				infoPanel, treePanel);
		splitPane.setDividerSize(3);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
	}

	/*
	 * Initialize menu items
	 */
	private void initMenu() {
		treePopup.add(new JMenuItem(deleteAttributeAction));
	}

	/*
	 * Register listeners
	 */
	private void registerListeners() {

		attributeTree.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					treePath = attributeTree.getPathForLocation(e.getX(), e
							.getY());
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
							.getLastPathComponent();
					if (node.getChildCount() == 0)
						return;
					treePopup.show(attributeTree, e.getX(), e.getY());

				}
				if (SwingUtilities.isLeftMouseButton(e)
						&& e.getClickCount() > 1) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) attributeTree
							.getLastSelectedPathComponent();
					// only leafs(values) are editable
					if (node.isLeaf()) {
						editNode(node);
					}
				}
			}
		});

	}

	protected void editNode(DefaultMutableTreeNode node) {
		String origValue = node.getUserObject().toString();
		String newValue;
		Object value = null;
		while (null == value) {
			newValue = JOptionPane.showInputDialog(this,
					"New attribute value ?", origValue);
			if (null == newValue)
				return;
			value = getValue(node, newValue);
		}
		node.setUserObject(value);
		attributeTreeModel.nodeChanged(node);
	}

	private Object getValue(DefaultMutableTreeNode node, String newValue) {
		if (node.getUserObject() instanceof XMLGregorianCalendar) {
			try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(
						newValue);
			} catch (DatatypeConfigurationException e) {
				JOptionPane.showMessageDialog(this, "Invalid input");
				return null;
			}
		} else if (node.getUserObject() instanceof Boolean) {
			if (newValue.equals("true") || newValue.equals("false"))
				return new Boolean(newValue);
			else {
				JOptionPane
						.showMessageDialog(this,
								"Invalid input, valid inputs for this Boolean are: true/false");
				return null;
			}
		} else if (node.getUserObject() instanceof Integer) {
			try {
				Integer.parseInt(newValue);
				return new Integer(newValue);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Invalid input, not a valid integer value");
				return null;
			}
		} else if (node.getUserObject() instanceof Double) {
			try {
				Double.parseDouble(newValue);
				return new Double(newValue);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Invalid input, not a valid double value");
				return null;
			}
		} else {
			return newValue;
		}
	}

	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {
		if (arg instanceof Map) { // from attribute web service
			addMap(userNode, (Map<String, Object>) arg);
		} else if (arg instanceof Boolean) { // from data web service
		} else if (arg instanceof String) { // error from one of both
			JOptionPane.showMessageDialog(this, (String) arg, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/*
	 * Add user node to the attribute tree
	 */
	public void addUser(String user) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) attributeTreeModel
				.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root
					.getChildAt(i);
			if (node.getUserObject().toString().equals(user)) {
				attributeTreeModel.removeNodeFromParent(node);
			}
		}
		userNode = new DefaultMutableTreeNode(user);
		attributeTreeModel.insertNodeInto(userNode, root, root.getChildCount());
		attributeTree.expandRow(0);
	}

	/*
	 * Add list of attributes to a root node in the attribute tree
	 */
	private void addMap(DefaultMutableTreeNode root, Map<String, Object> map) {
		HashMap<String, Object> data = (HashMap<String, Object>) map;

		for (Iterator<String> it = data.keySet().iterator(); it.hasNext();) {
			String attributeName = (String) it.next();
			Object attributeValue = (Object) data.get(attributeName);

			DefaultMutableTreeNode attributeNameNode = new DefaultMutableTreeNode(
					attributeName);
			attributeTreeModel.insertNodeInto(attributeNameNode, root, root
					.getChildCount());
			if (attributeValue.getClass().isArray()) {
				addMultiValueAttribute(attributeNameNode, attributeValue);
			} else {
				addSingleValueAttribute(attributeNameNode, attributeValue);
			}

			// expand complete tree
			for (int i = 0; i < attributeTree.getRowCount(); i++)
				attributeTree.expandRow(i);
		}
	}

	private void addSingleValueAttribute(
			DefaultMutableTreeNode attributeNameNode, Object attributeValue) {
		if (attributeValue instanceof AttributeType)
			addCompoundAttribute(attributeNameNode, attributeValue);
		else {
			DefaultMutableTreeNode attributeValueNode = new DefaultMutableTreeNode(
					attributeValue);
			attributeTreeModel.insertNodeInto(attributeValueNode,
					attributeNameNode, attributeNameNode.getChildCount());
		}
	}

	private void addMultiValueAttribute(
			DefaultMutableTreeNode attributeNameNode, Object attributeValues) {
		for (Object attributeValue : (Object[]) attributeValues) {
			addSingleValueAttribute(attributeNameNode, attributeValue);
		}
	}

	private void addCompoundAttribute(DefaultMutableTreeNode attributeNameNode,
			Object attributeValue) {
		AttributeType attributeType = (AttributeType) attributeValue;
		DefaultMutableTreeNode attributeCompoundNameNode = new DefaultMutableTreeNode(
				attributeType.getName());
		attributeTreeModel.insertNodeInto(attributeCompoundNameNode,
				attributeNameNode, attributeNameNode.getChildCount());
		List<Object> attributeCompoundValues = attributeType
				.getAttributeValue();
		for (Object attributeCompoundValue : attributeCompoundValues) {
			addSingleValueAttribute(attributeCompoundNameNode,
					attributeCompoundValue);
		}
	}

	/*
	 * Get complete attribute value's ( single/multi ) from a given attribute
	 * name node
	 */
	private Object getAttributeValue(DefaultMutableTreeNode node) {
		if (node.getChildCount() > 1) { // multi-valued
			Object[] attributeValues = new Object[node.getChildCount()];
			for (int i = 0; i < node.getChildCount(); i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
						.getChildAt(i);
				attributeValues[i] = child.getUserObject();
			}
			return attributeValues;
		} else { // single-valued
			if (node.getChildCount() == 0)
				return node.getUserObject();
			node = (DefaultMutableTreeNode) node.getChildAt(0);
			return node.getUserObject();
		}
	}

	/*
	 * 
	 * Tree model listener class to catch leaf edits
	 * 
	 * Upon an edit, the attribute is immediately updated through the SafeOnline
	 * web service
	 * 
	 */
	private class AttributeTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode) (e.getTreePath()
					.getLastPathComponent());

			if (e.getTreePath().getPathCount() < 3)
				return;
			String userName = (String) ((DefaultMutableTreeNode) e.getPath()[1])
					.getUserObject();
			String attributeName = (String) ((DefaultMutableTreeNode) e
					.getPath()[2]).getUserObject();
			Object attributeValue = getAttributeValue(node);

			if (null == attributeValue)
				return;
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			LOG.info("Attribute value type : "
					+ attributeValue.getClass().toString());

			ServicesUtils.getInstance().setAttributeValue(userName,
					attributeName, attributeValue);
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	/*
	 * 
	 * Action classes
	 * 
	 */
	private class GetAttributesAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public GetAttributesAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
		}

		public void actionPerformed(ActionEvent evt) {
			if (null == userField.getText())
				return;
			String user = userField.getText();
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			ServicesUtils.getInstance().getAttributes(user);
			addUser(user);
		}
	}

	private class DeleteAttributeAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DeleteAttributeAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
		}

		public void actionPerformed(ActionEvent evt) {
			if (treePath == null)
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
					.getLastPathComponent();
			String user = userField.getText();
			String attributeName = (String) node.getUserObject();
			Object attributeValue = getAttributeValue(node);
			String attributeId = null;
			if (CompoundUtil.isCompound(attributeValue))
				attributeId = CompoundUtil.getAttributeId(attributeValue);

			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			ServicesUtils.getInstance().removeAttribute(user, attributeName,
					attributeId);
		}
	}

	private class AddAttributeAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AddAttributeAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
		}

		public void actionPerformed(ActionEvent evt) {
			String user = userField.getText();

			// new AddAttribute(user);

			String attributeName = JOptionPane
					.showInputDialog("Please give the name of the new attribute");
			if (null == attributeName || attributeName.equals(""))
				return;

			String[] attributeTypes = { "Boolean", "String", "Integer",
					"Double", "Date" };
			int answer = JOptionPane.showOptionDialog(new JFrame(),
					"Please select the type of attribute :", "",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, attributeTypes, attributeTypes[0]);
			if (answer == JOptionPane.CANCEL_OPTION)
				return;

			Object attributeValue = null;
			if (attributeTypes[answer].equals("Boolean")) {
				String[] booleanValues = { "true", "false" };
				int boolAnswer = JOptionPane.showOptionDialog(new JFrame(),
						"What value for attribute " + attributeName + " ?", "",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, booleanValues,
						booleanValues[0]);
				if (boolAnswer == JOptionPane.CANCEL_OPTION)
					return;
				else if (boolAnswer == 0)
					attributeValue = new Boolean(true);
				else
					attributeValue = new Boolean(false);
			} else {

			}

			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			ServicesUtils.getInstance().createAttribute(user, attributeName,
					attributeValue);

		}
	}

	private class DoneAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DoneAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			parent.resetContent();
		}
	}

}
