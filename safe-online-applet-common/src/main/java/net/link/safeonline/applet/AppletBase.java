/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.link.safeonline.p11sc.SmartCard;

import org.apache.commons.logging.Log;

/**
 * The base class for both the identity and the authentication applet.
 * 
 * @author fcorneli
 */
public abstract class AppletBase extends JApplet implements ActionListener,
		AppletView, RuntimeContext, StatementProvider {

	private static final long serialVersionUID = 1L;

	private JTextArea outputArea;

	private JLabel infoLabel;

	private InfoLevel infoLevel;

	private JProgressBar progressBar;

	private JPanel cards;

	private JButton hideButton;

	private static enum State {
		HIDE, SHOW
	};

	private State state = State.HIDE;

	private ResourceBundle messages;

	@Override
	public void init() {

		Locale locale = getLocale();
		this.messages = ResourceBundle.getBundle(
				"net.link.safeonline.applet.AppletMessages", locale);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					setupScreen();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("could not setup the GUI");
		}
		initAppletController();
	}

	private void initAppletController() {
		AppletControl appletControl = new AppletControl(this, this, this);
		Thread thread = new Thread(appletControl);
		thread.start();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	private void setupScreen() {

		setLayout(new BorderLayout());
		Container container = getContentPane();

		JPanel infoPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS);
		infoPanel.setLayout(boxLayout);
		container.add(infoPanel, BorderLayout.NORTH);
		this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
		this.progressBar.setIndeterminate(true);
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.infoLabel = new JLabel();
		Font font = this.infoLabel.getFont();
		font = font.deriveFont((float) 16);
		this.infoLabel.setFont(font);
		this.infoLabel.setText(this.messages.getString("starting"));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.hideButton = new JButton();
		this.hideButton.addActionListener(this);
		textPanel.add(this.infoLabel);
		buttonPanel.add(this.hideButton);
		infoPanel.add(textPanel);
		infoPanel.add(this.progressBar);
		infoPanel.add(Box.createVerticalStrut(10));
		infoPanel.add(buttonPanel);
		infoPanel.add(Box.createVerticalStrut(10));

		this.cards = new JPanel(new CardLayout());
		this.outputArea = new JTextArea();
		this.outputArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.outputArea);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.cards.add(scrollPane, "details");
		JPanel emptyPanel = new JPanel();
		this.cards.add(emptyPanel, "empty");
		container.add(this.cards, BorderLayout.CENTER);

		// syncing state
		actionPerformed(null);
		actionPerformed(null);

		// background color
		String c = getParameter("bgcolor");
		Color color = null;
		try {
			color = Color.decode(c);
		} catch (Exception e) {
			color = Color.WHITE;
		}
		iterateBackground(container.getComponents(), color);

	}

	public void outputDetailMessage(final String message) {
		/*
		 * We used to have invokeAndWait here, but this sometimes causes a
		 * deadlock between: RunnableQueue-0 and AWT-EventQueue-0.
		 */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AppletBase.this.outputArea.append(message + "\n");
			}
		});
	}

	public void outputInfoMessage(final InfoLevel infoLevel,
			final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AppletBase.this.infoLabel.setText(message);
				if (AppletBase.this.infoLevel != infoLevel) {
					AppletBase.this.infoLevel = infoLevel;
					switch (infoLevel) {
					case NORMAL:
						AppletBase.this.infoLabel.setForeground(Color.BLACK);
						AppletBase.this.progressBar.setIndeterminate(true);
						break;
					case ERROR:
						AppletBase.this.infoLabel.setForeground(Color.RED);
						AppletBase.this.progressBar.setIndeterminate(false);
						break;
					}
				}
			}
		});
	}

	public abstract byte[] createStatement(SmartCard smartCard);

	private void setButtonLabel(JButton button) {
		if (this.state == State.HIDE) {
			button.setText(this.messages.getString("showDetails"));
		} else {
			button.setText(this.messages.getString("hideDetails"));
		}
	}

	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout) (this.cards.getLayout());
		if (this.state == State.HIDE) {
			cl.show(this.cards, "details");
			this.state = State.SHOW;
			setButtonLabel(this.hideButton);
		} else {
			cl.show(this.cards, "empty");
			this.state = State.HIDE;
			setButtonLabel(this.hideButton);
		}
	}

	private void iterateBackground(Component[] components, Color color) {
		for (Component comp : components) {
			if (comp instanceof Container) {
				Container container = (Container) comp;
				iterateBackground(container.getComponents(), color);
			}
			comp.setBackground(color);
		}
	}

	public Log getLog() {
		Log log = new AppletLog(this);
		return log;
	}

	public void showDocument(URL url) {
		AppletContext appletContext = getAppletContext();
		appletContext.showDocument(url);
	}

	@Override
	public Locale getLocale() {
		String language = getParameter("Language");
		if (null != language) {
			Locale locale = new Locale(language);
			return locale;
		}
		return super.getLocale();
	}
}
