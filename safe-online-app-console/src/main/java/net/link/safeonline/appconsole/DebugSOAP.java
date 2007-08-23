package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.CAPTURE;
import static net.link.safeonline.appconsole.Messages.DEBUG;
import static net.link.safeonline.appconsole.Messages.QUIT;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;

import net.link.safeonline.sdk.DomUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class DebugSOAP extends JFrame implements Observer {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(DebugSOAP.class);

	private boolean inputMessage = true;
	/*
	 * Actions
	 */
	private Action quitAction = new QuitAction(QUIT.getMessage());
	private Action captureAction = new CaptureAction(CAPTURE.getMessage());

	private JTextArea inMessageArea = new JTextArea();
	private JTextArea outMessageArea = new JTextArea();

	/**
	 * Main constructor.
	 */
	public DebugSOAP() {
		super(DEBUG.getMessage());

		buildWindow();

		ApplicationConsoleManager.getInstance().addObserver(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 300);
		this.setLocation(200, 200);
		this.setVisible(true);
	}

	private void buildWindow() {
		JScrollPane inputScroll = new JScrollPane(inMessageArea);
		inputScroll.setBorder(new TitledBorder("In"));
		inMessageArea.setEditable(false);
		JScrollPane outputScroll = new JScrollPane(outMessageArea);
		outputScroll.setBorder(new TitledBorder("Out"));
		outMessageArea.setEditable(false);

		JSplitPane messagePanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				outputScroll, inputScroll);
		messagePanel.setDividerSize(3);
		messagePanel.setResizeWeight(0.5);

		JPanel controlPanel = new JPanel(new FlowLayout());
		JCheckBox captureBox = new JCheckBox(captureAction);
		captureBox.setSelected(true);
		controlPanel.add(captureBox);
		controlPanel.add(new JButton(quitAction));

		this.add(messagePanel, BorderLayout.CENTER);
		this.add(controlPanel, BorderLayout.SOUTH);
	}

	private void closeFrame() {
		this.dispose();
	}

	public void update(Observable o, Object arg) {
		if (null == arg)
			return;
		if (arg instanceof Document) {
			Document doc = (Document) arg;
			if (inputMessage) {
				try {
					inMessageArea.setText(DomUtils.domToString(doc));
				} catch (TransformerException e) {
					inMessageArea
							.setText("Failed to transform SOAP message !!");
					LOG.error("TransformerException thrown", e);
				}
				inputMessage = false;
			} else {
				try {
					outMessageArea.setText(DomUtils.domToString(doc));
				} catch (TransformerException e) {
					outMessageArea
							.setText("Failed to transform SOAP message !!");
					LOG.error("TransformerException thrown", e);
				}
				inputMessage = true;
			}
		}
	}

	/*
	 * Action classes
	 */
	public class CaptureAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public CaptureAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() instanceof JCheckBox) {
				JCheckBox value = (JCheckBox) evt.getSource();
				ApplicationConsoleManager.getInstance().setCaptureMessages(
						value.isSelected());
			}
		}

	}

	public class QuitAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public QuitAction(String name) {
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, name);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
		}

		public void actionPerformed(ActionEvent evt) {
			closeFrame();
		}
	}

}
