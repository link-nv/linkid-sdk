/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProviderException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;
import net.link.safeonline.p11sc.impl.SmartCardImpl;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.logging.Log;

/**
 * The identity applet creates an identity statement at the client-side within
 * the browser.
 * 
 * @author fcorneli
 * 
 */
public class IdentityApplet extends JApplet implements Runnable {

	private static final long serialVersionUID = 1L;

	private JTextArea outputArea;

	private JLabel infoLabel;

	private InfoLevel infoLevel;

	private JProgressBar progressBar;

	public IdentityApplet() {
		setupScreen();
		new Thread(this).start();
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

	private void outputDetailMessage(final String message) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					IdentityApplet.this.outputArea.append(message + "\n");
				}
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
		}
	}

	private static enum InfoLevel {
		NORMAL, ERROR
	};

	private void outputInfoMessage(final InfoLevel infoLevel,
			final String message) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					IdentityApplet.this.infoLabel.setText(message);
					if (IdentityApplet.this.infoLevel != infoLevel) {
						IdentityApplet.this.infoLevel = infoLevel;
						switch (infoLevel) {
						case NORMAL:
							IdentityApplet.this.infoLabel
									.setForeground(Color.BLACK);
							IdentityApplet.this.progressBar
									.setIndeterminate(true);
							break;
						case ERROR:
							IdentityApplet.this.infoLabel
									.setForeground(Color.RED);
							IdentityApplet.this.progressBar
									.setIndeterminate(false);
							break;
						}
					}
				}
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
		}
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
			return;
		}

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		outputDetailMessage("given name: " + givenName);
		outputDetailMessage("surname: " + surname);

		String user = getParameter("User");

		outputInfoMessage(InfoLevel.NORMAL, "Creating identity statement...");
		outputDetailMessage("Creating identity statement for user " + user
				+ "...");
		byte[] identityStatement;
		try {
			identityStatement = IdentityStatementFactory
					.createIdentityStatement(user, smartCard);
		} catch (ProviderException e) {
			outputInfoMessage(InfoLevel.ERROR,
					"Could not sign the identity statement.");
			outputDetailMessage("error signing the identity statement: "
					+ e.getMessage());
			return;
		} catch (Exception e) {
			outputInfoMessage(InfoLevel.ERROR,
					"Could not create the identity statement.");
			outputDetailMessage("error creating the identity statement: "
					+ e.getMessage());
			return;
		}

		outputDetailMessage("Disconnecting from smart card...");
		smartCard.close();

		try {
			if (false == sendIdentityStatement(identityStatement)) {
				return;
			}
		} catch (IOException e) {
			outputDetailMessage("Error occurred while sending the identity statement");
			outputDetailMessage("IO error: " + e.getMessage());
			outputInfoMessage(InfoLevel.ERROR,
					"Error sending the identity statement.");
			return;
		} catch (Exception e) {
			outputDetailMessage("Error occurred while sending the identity statement");
			outputDetailMessage("Error: " + e.getMessage());
			outputInfoMessage(InfoLevel.ERROR,
					"Error sending the identity statement.");
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

	private void setupLogging(SmartCard smartCard) {
		Log log = new AppletLog();
		SmartCardImpl.setLog(log);
	}

	private class AppletLog implements Log {

		public void debug(Object message) {
			IdentityApplet.this.outputDetailMessage("DEBUG: " + message);
		}

		public void debug(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("DEBUG: " + message);
		}

		public void error(Object message) {
			IdentityApplet.this.outputDetailMessage("ERROR: " + message);
		}

		public void error(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("ERROR: " + message);
		}

		public void fatal(Object message) {
			IdentityApplet.this.outputDetailMessage("FATAL: " + message);
		}

		public void fatal(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("FATAL: " + message);
		}

		public void info(Object message) {
			IdentityApplet.this.outputDetailMessage("INFO: " + message);
		}

		public void info(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("INFO: " + message);
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
			IdentityApplet.this.outputDetailMessage("TRACE: " + message);
		}

		public void trace(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("TRACE: " + message);
		}

		public void warn(Object message) {
			IdentityApplet.this.outputDetailMessage("WARN: " + message);
		}

		public void warn(Object message, Throwable t) {
			IdentityApplet.this.outputDetailMessage("WARN: " + message);
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

	private boolean sendIdentityStatement(byte[] identityStatement)
			throws IOException {
		outputInfoMessage(InfoLevel.NORMAL, "Sending identity statement...");
		outputDetailMessage("Sending identity statement...");
		URL documentBase = getDocumentBase();
		outputDetailMessage("document base: " + documentBase);
		String identityServletPath = this.getParameter("IdentityServletPath");
		URL url = transformUrl(documentBase, identityServletPath);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setRequestProperty("Content-type",
				"application/octet-stream");
		httpURLConnection.setDoOutput(true);
		OutputStream outputStream = httpURLConnection.getOutputStream();
		outputStream.write(identityStatement);
		outputStream.close();
		httpURLConnection.connect();

		httpURLConnection.disconnect();

		int responseCode = httpURLConnection.getResponseCode();
		if (200 == responseCode) {
			outputDetailMessage("Identity statement successfully transmitted.");
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
		throw new IOException("Response code: " + responseCode);
	}
}
