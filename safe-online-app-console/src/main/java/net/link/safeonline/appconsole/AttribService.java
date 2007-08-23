package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CANCEL;
import static net.link.safeonline.appconsole.Messages.GET_ATTRIBUTES;
import static net.link.safeonline.appconsole.Messages.USER;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.collections.set.CompositeSet.SetMutator;

public class AttribService extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;

	private ApplicationConsole parent = null;

	/*
	 * Actions
	 */
	private Action getAttributesAction = new GetAttributesAction(GET_ATTRIBUTES
			.getMessage());
	private Action cancelAction = new CancelAction(CANCEL.getMessage());

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

	public AttribService(ApplicationConsole applicationConsole) {
		this.parent = applicationConsole;
		ServicesUtils.getInstance().addObserver(this);
		buildWindow();
	}

	private void buildWindow() {
		JPanel infoPanel = new JPanel();
		JScrollPane treePanel = new JScrollPane(attributeTree);

		infoPanel.setLayout(new FlowLayout());
		infoPanel.add(userLabel);
		infoPanel.add(userField);
		JButton getAttributesButton = new JButton(getAttributesAction);
		getAttributesButton.setMultiClickThreshhold(1000); // avoid impatient
		// users
		infoPanel.add(getAttributesButton);
		infoPanel.add(new JButton(cancelAction));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				infoPanel, treePanel);
		splitPane.setDividerSize(3);
		splitPane.setResizeWeight(1.0);
		this.add(splitPane);
		userField.requestFocusInWindow();
	}

	public void update(Observable o, Object arg) {
		if (arg instanceof Map) {
			addMap(userNode, (Map<String, Object>) arg);
			
		}
	}

	public void addUser(String user) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) attributeTreeModel
				.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root
					.getChildAt(i);
			if (node.getUserObject().toString().equals(user)) {
				userNode = node;
				return;
			}
		}
		userNode = new DefaultMutableTreeNode(user);
		attributeTreeModel.insertNodeInto(userNode, root, root.getChildCount());
		attributeTree.expandRow(0);
	}

	private void addMap(DefaultMutableTreeNode root, Map<String, Object> map) {
		HashMap<String, Object> data = (HashMap<String, Object>) map;

		for (Iterator<String> i = data.keySet().iterator(); i.hasNext();) {
			String attributeName = (String) i.next();
			Object attributeValue = (Object) data.get(attributeName);
			if (attributeValue.getClass().isArray()) {
				if (attributeValue instanceof Map) {
					addMap(root, (Map<String, Object>) attributeValue);
				} else {
					DefaultMutableTreeNode attributeNameNode = new DefaultMutableTreeNode(
							attributeName);
					attributeTreeModel.insertNodeInto(attributeNameNode, root,
							root.getChildCount());
					for (Object t : (Object[]) attributeValue) {
						DefaultMutableTreeNode attributeNode = new DefaultMutableTreeNode(
								t);
						attributeTreeModel.insertNodeInto(attributeNode,
								attributeNameNode, attributeNameNode
										.getChildCount());
					}
				}
			} else {
				DefaultMutableTreeNode attributeNameNode = new DefaultMutableTreeNode(
						attributeName);
				attributeTreeModel.insertNodeInto(attributeNameNode, root, root
						.getChildCount());
				DefaultMutableTreeNode attributeValueNode = new DefaultMutableTreeNode(
						attributeValue);
				attributeTreeModel.insertNodeInto(attributeValueNode,
						attributeNameNode, attributeNameNode.getChildCount());
			}

		}
	}

	/*
	 * 
	 * Action classes
	 * 
	 */
	public class GetAttributesAction extends AbstractAction {

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
			ServicesUtils.getInstance().getAttributes(user);
			addUser(user);
		}

	}

	public class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CancelAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			parent.resetContent();
		}
	}

}
