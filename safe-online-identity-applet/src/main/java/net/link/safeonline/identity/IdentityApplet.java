/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.applet.AppletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;
import net.link.safeonline.p11sc.impl.SmartCardImpl;

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

	public IdentityApplet() {
		setupScreen();
		new Thread(this).start();
	}

	private void setupScreen() {
		this.outputArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(this.outputArea);
		add(scrollPane);
	}

	private void output(final String message) {
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

	public void run() {
		output("Loading smart card component...");
		SmartCard smartCard = SmartCardFactory.newInstance();

		setupLogging(smartCard);

		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		List<SmartCardConfig> smartCardConfigs = configFactory
				.getSmartCardConfigs();
		smartCard.init(smartCardConfigs);
		for (SmartCardConfig smartCardConfig : smartCardConfigs) {
			output("smart card config available for: "
					+ smartCardConfig.getCardAlias());
		}

		String smartCardAlias = getParameter("SmartCardConfig");

		output("Connecting to smart card...");
		String osName = System.getProperty("os.name");
		output("os name: " + osName);
		try {
			smartCard.open(smartCardAlias);
		} catch (Exception e) {
			output("error opening the smart card: " + e.getMessage());
			output("error type: " + e.getClass().getName());
			return;
		}

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		output("given name: " + givenName);
		output("surname: " + surname);

		String user = getParameter("User");

		output("Creating identity statement for user " + user + "...");
		byte[] identityStatement = IdentityStatementFactory
				.createIdentityStatement(user, smartCard);

		output("Disconnecting from smart card...");
		smartCard.close();

		try {
			sendIdentityStatement(identityStatement);
		} catch (IOException e) {
			output("Error occurred while sending the identity statement");
			output("IO error: " + e.getMessage());
			return;
		}

		output("Done.");
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
			IdentityApplet.this.output("DEBUG: " + message);
		}

		public void debug(Object message, Throwable t) {
			IdentityApplet.this.output("DEBUG: " + message);
		}

		public void error(Object message) {
			IdentityApplet.this.output("ERROR: " + message);
		}

		public void error(Object message, Throwable t) {
			IdentityApplet.this.output("ERROR: " + message);
		}

		public void fatal(Object message) {
			IdentityApplet.this.output("FATAL: " + message);
		}

		public void fatal(Object message, Throwable t) {
			IdentityApplet.this.output("FATAL: " + message);
		}

		public void info(Object message) {
			IdentityApplet.this.output("INFO: " + message);
		}

		public void info(Object message, Throwable t) {
			IdentityApplet.this.output("INFO: " + message);
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
			IdentityApplet.this.output("TRACE: " + message);
		}

		public void trace(Object message, Throwable t) {
			IdentityApplet.this.output("TRACE: " + message);
		}

		public void warn(Object message) {
			IdentityApplet.this.output("WARN: " + message);
		}

		public void warn(Object message, Throwable t) {
			IdentityApplet.this.output("WARN: " + message);
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

	private void sendIdentityStatement(byte[] identityStatement)
			throws IOException {
		output("Sending identity statement...");
		URL documentBase = getDocumentBase();
		output("document base: " + documentBase);
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
			output("Identity statement successfully transmitted.");
			return;
		}
		if (httpURLConnection.getContentLength() > 0) {
			InputStream inputStream = httpURLConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			String message = bufferedReader.readLine();
			if (null != message) {
				output("Result message: " + message);
			}
		}
		throw new IOException("Response code: " + responseCode);
	}
}
