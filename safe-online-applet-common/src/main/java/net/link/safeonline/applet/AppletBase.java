/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProviderException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.SmartCardNotFoundException;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;
import net.link.safeonline.p11sc.impl.SmartCardImpl;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.logging.Log;

/**
 * The base class for both the identity and the authentication applet.
 * 
 * TODO: refactor and apply MVC pattern.
 * 
 * @author fcorneli
 * 
 */
public abstract class AppletBase extends JApplet implements Runnable,
		SmartCardPinCallback {

	private static final long serialVersionUID = 1L;

	private JTextArea outputArea;

	private JLabel infoLabel;

	private InfoLevel infoLevel;

	private JProgressBar progressBar;

	@Override
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					setupScreen();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("could not setup the GUI");
		}
		Thread thread = new Thread(this);
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
		this.infoLabel.setText("Starting...");
		textPanel.add(this.infoLabel);
		infoPanel.add(textPanel);
		infoPanel.add(this.progressBar);
		infoPanel.add(Box.createVerticalStrut(10));

		this.outputArea = new JTextArea();
		this.outputArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.outputArea);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		container.add(scrollPane, BorderLayout.CENTER);
	}

	protected void outputDetailMessage(final String message) {
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

	protected static enum InfoLevel {
		NORMAL, ERROR
	};

	protected void outputInfoMessage(final InfoLevel infoLevel,
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

	public void run() {
		outputInfoMessage(InfoLevel.NORMAL, "Connecting to smart card...");
		outputDetailMessage("Loading smart card component...");
		SmartCard smartCard = SmartCardFactory.newInstance();

		setupLogging(smartCard);

		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		List<SmartCardConfig> smartCardConfigs = configFactory
				.getSmartCardConfigs();
		smartCard.init(smartCardConfigs);
		for (SmartCardConfig smartCardConfig : smartCardConfigs) {
			outputDetailMessage("smart card config available for: "
					+ smartCardConfig.getCardAlias());
		}

		String smartCardAlias = getParameter("SmartCardConfig");

		outputDetailMessage("Connecting to smart card...");
		String osName = System.getProperty("os.name");
		outputDetailMessage("os name: " + osName);

		smartCard.setSmartCardPinCallback(this);

		try {
			smartCard.open(smartCardAlias);
		} catch (SmartCardNotFoundException e) {
			outputDetailMessage("smart card not found");
			outputInfoMessage(InfoLevel.ERROR, "Smart card not found.");
			return;
		} catch (Exception e) {
			outputDetailMessage("error opening the smart card: "
					+ e.getMessage());
			outputDetailMessage("error type: " + e.getClass().getName());
			outputInfoMessage(InfoLevel.ERROR,
					"Could not connect to the smart card.");
			for (StackTraceElement stackTraceElement : e.getStackTrace()) {
				outputDetailMessage(stackTraceElement.getClassName() + "."
						+ stackTraceElement.getMethodName() + " ("
						+ stackTraceElement.getFileName() + ":"
						+ stackTraceElement.getLineNumber() + ")");
			}
			return;
		}

		byte[] statement;
		try {
			statement = createStatement(smartCard);
		} catch (ProviderException e) {
			outputInfoMessage(InfoLevel.ERROR, "Could not sign the statement.");
			outputDetailMessage("error signing the statement: "
					+ e.getMessage());
			return;
		} catch (Exception e) {
			outputInfoMessage(InfoLevel.ERROR,
					"Could not create the statement.");
			outputDetailMessage("error creating the statement: "
					+ e.getMessage());
			return;
		} finally {
			outputDetailMessage("Disconnecting from smart card...");
			smartCard.close();
		}

		try {
			if (false == sendStatement(statement)) {
				return;
			}
		} catch (IOException e) {
			outputDetailMessage("Error occurred while sending the statement");
			outputDetailMessage("IO error: " + e.getMessage());
			outputInfoMessage(InfoLevel.ERROR, "Error sending the statement.");
			return;
		} catch (Exception e) {
			outputDetailMessage("Error occurred while sending the statement");
			outputDetailMessage("Error: " + e.getMessage());
			outputInfoMessage(InfoLevel.ERROR, "Error sending the statement.");
			return;
		}
		outputInfoMessage(InfoLevel.NORMAL, "Done.");

		outputDetailMessage("Done.");
		AppletContext appletContext = getAppletContext();
		URL documentBase = getDocumentBase();
		String targetPath = getParameter("TargetPath");
		URL target = transformUrl(documentBase, targetPath);
		appletContext.showDocument(target);
	}

	protected abstract byte[] createStatement(SmartCard smartCard);

	private void setupLogging(SmartCard smartCard) {
		Log log = new AppletLog();
		SmartCardImpl.setLog(log);
	}

	private class AppletLog implements Log {

		public void debug(Object message) {
			AppletBase.this.outputDetailMessage("DEBUG: " + message);
		}

		public void debug(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("DEBUG: " + message);
		}

		public void error(Object message) {
			AppletBase.this.outputDetailMessage("ERROR: " + message);
		}

		public void error(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("ERROR: " + message);
		}

		public void fatal(Object message) {
			AppletBase.this.outputDetailMessage("FATAL: " + message);
		}

		public void fatal(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("FATAL: " + message);
		}

		public void info(Object message) {
			AppletBase.this.outputDetailMessage("INFO: " + message);
		}

		public void info(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("INFO: " + message);
		}

		public boolean isDebugEnabled() {
			return true;
		}

		public boolean isErrorEnabled() {
			return true;
		}

		public boolean isFatalEnabled() {
			return true;
		}

		public boolean isInfoEnabled() {
			return true;
		}

		public boolean isTraceEnabled() {
			return true;
		}

		public boolean isWarnEnabled() {
			return true;
		}

		public void trace(Object message) {
			AppletBase.this.outputDetailMessage("TRACE: " + message);
		}

		public void trace(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("TRACE: " + message);
		}

		public void warn(Object message) {
			AppletBase.this.outputDetailMessage("WARN: " + message);
		}

		public void warn(Object message, Throwable t) {
			AppletBase.this.outputDetailMessage("WARN: " + message);
		}
	}

	public static URL transformUrl(URL documentBase, String targetPath) {
		String documentBaseStr = documentBase.toString();
		int idx = documentBaseStr.lastIndexOf("/");
		String identityUrlStr = documentBaseStr.substring(0, idx + 1)
				+ targetPath;
		try {
			return new URL(identityUrlStr);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL error: " + e.getMessage());
		}
	}

	private boolean sendStatement(byte[] statement) throws IOException {
		outputInfoMessage(InfoLevel.NORMAL, "Sending statement...");
		outputDetailMessage("Sending statement...");
		URL documentBase = getDocumentBase();
		outputDetailMessage("document base: " + documentBase);
		String servletPath = this.getParameter("ServletPath");
		URL url = transformUrl(documentBase, servletPath);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setRequestProperty("Content-type",
				"application/octet-stream");
		httpURLConnection.setDoOutput(true);
		OutputStream outputStream = httpURLConnection.getOutputStream();
		outputStream.write(statement);
		outputStream.close();
		httpURLConnection.connect();

		httpURLConnection.disconnect();

		int responseCode = httpURLConnection.getResponseCode();
		if (200 == responseCode) {
			outputDetailMessage("Statement successfully transmitted.");
			return true;
		}
		String safeOnlineResultCode = httpURLConnection
				.getHeaderField(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER);
		if (SharedConstants.PERMISSION_DENIED_ERROR
				.equals(safeOnlineResultCode)) {
			outputDetailMessage("PERMISSION DENIED. YOUR EID MIGHT BE IN USE BY ANOTHER USER");
			outputInfoMessage(InfoLevel.ERROR, "Permission denied.");
			return false;
		}
		if (SharedConstants.SUBSCRIPTION_NOT_FOUND_ERROR
				.equals(safeOnlineResultCode)) {
			outputInfoMessage(InfoLevel.ERROR,
					"You are not subscribed to the application.");
			return false;
		}
		if (SharedConstants.SUBJECT_NOT_FOUND_ERROR
				.equals(safeOnlineResultCode)) {
			outputInfoMessage(InfoLevel.ERROR,
					"You did not register your eID card with SafeOnline.");
			return false;
		}
		throw new IOException("Response code: " + responseCode);
	}

	public char[] getPin() {
		JLabel promptLabel = new JLabel("Give your PIN:");

		JPasswordField passwordField = new JPasswordField(8);
		passwordField.setEchoChar('*');

		Box passwordPanel = Box.createHorizontalBox();
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		passwordPanel.add(passwordField);

		int result = JOptionPane.showOptionDialog(null, passwordPanel,
				"PIN Required", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			char[] pin = passwordField.getPassword();
			return pin;
		}
		return null;
	}
}
