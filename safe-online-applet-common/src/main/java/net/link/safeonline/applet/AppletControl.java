/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProviderException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

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

import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Applet control component.
 * 
 * @author fcorneli
 * 
 */
public class AppletControl implements Runnable, SmartCardPinCallback {

	private final AppletView appletView;

	private final RuntimeContext runtimeContext;

	private final StatementProvider statementProvider;

	private final ResourceBundle messages;

	public AppletControl(final AppletView appletView,
			final RuntimeContext runtimeContext,
			final StatementProvider statementProvider) {
		this.appletView = appletView;
		this.runtimeContext = runtimeContext;
		this.statementProvider = statementProvider;

		Locale locale = this.runtimeContext.getLocale();
		this.messages = ResourceBundle.getBundle(
				"net.link.safeonline.applet.ControlMessages", locale);
	}

	private void setupLogging(@SuppressWarnings("unused")
	SmartCard smartCard) {
		Log log = this.appletView.getLog();
		SmartCardImpl.setLog(log);
	}

	public void run() {
		this.appletView.outputInfoMessage(InfoLevel.NORMAL, this.messages
				.getString("connectingToSmartCard"));
		this.appletView.outputDetailMessage("Loading smart card component...");
		SmartCard smartCard = SmartCardFactory.newInstance();

		setupLogging(smartCard);

		SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
		List<SmartCardConfig> smartCardConfigs = configFactory
				.getSmartCardConfigs();
		smartCard.init(smartCardConfigs);
		for (SmartCardConfig smartCardConfig : smartCardConfigs) {
			this.appletView
					.outputDetailMessage("smart card config available for: "
							+ smartCardConfig.getCardAlias());
		}

		String smartCardAlias = this.runtimeContext
				.getParameter("SmartCardConfig");

		this.appletView.outputDetailMessage("Connecting to smart card...");
		String osName = System.getProperty("os.name");
		this.appletView.outputDetailMessage("os name: " + osName);

		smartCard.setSmartCardPinCallback(this);

		try {
			smartCard.open(smartCardAlias);
		} catch (SmartCardNotFoundException e) {
			this.appletView.outputDetailMessage("smart card not found");
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("smartCardNotFound"));
			return;
		} catch (Exception e) {
			this.appletView
					.outputDetailMessage("error opening the smart card: "
							+ e.getMessage());
			this.appletView.outputDetailMessage("error type: "
					+ e.getClass().getName());
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("smartCardConnectError"));
			for (StackTraceElement stackTraceElement : e.getStackTrace()) {
				this.appletView.outputDetailMessage(stackTraceElement
						.getClassName()
						+ "."
						+ stackTraceElement.getMethodName()
						+ " ("
						+ stackTraceElement.getFileName()
						+ ":"
						+ stackTraceElement.getLineNumber() + ")");
			}
			return;
		}

		byte[] statement;
		try {
			statement = this.statementProvider.createStatement(smartCard);
		} catch (ProviderException e) {
			Throwable cause = e.getCause();
			if (cause instanceof PKCS11Exception) {
				smartCard.close();
				smartCard.resetPKCS11Driver();
				try {
					smartCard.open(smartCardAlias);
					statement = this.statementProvider
							.createStatement(smartCard);
				} catch (Exception e2) {
					this.appletView.outputInfoMessage(InfoLevel.ERROR,
							this.messages.getString("signErrorMsg"));
					this.appletView
							.outputDetailMessage("error signing the statement: "
									+ e2.getMessage());
					return;
				}
			} else {
				this.appletView.outputInfoMessage(InfoLevel.ERROR,
						this.messages.getString("signErrorMsg"));
				this.appletView
						.outputDetailMessage("error signing the statement: "
								+ e.getMessage());
				return;
			}
		} catch (Exception e) {
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("statementError"));
			this.appletView
					.outputDetailMessage("error creating the statement: "
							+ e.getMessage());
			return;
		} finally {
			this.appletView
					.outputDetailMessage("Disconnecting from smart card...");
			smartCard.close();
		}

		try {
			if (false == sendStatement(statement)) {
				return;
			}
		} catch (IOException e) {
			this.appletView
					.outputDetailMessage("Error occurred while sending the statement");
			this.appletView.outputDetailMessage("IO error: " + e.getMessage());
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("errorSending"));
			return;
		} catch (Exception e) {
			this.appletView
					.outputDetailMessage("Error occurred while sending the statement");
			this.appletView.outputDetailMessage("Error: " + e.getMessage());
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("errorSending"));
			return;
		}
		this.appletView.outputInfoMessage(InfoLevel.NORMAL, this.messages
				.getString("done"));

		this.appletView.outputDetailMessage("Done.");

		URL documentBase = this.runtimeContext.getDocumentBase();
		String targetPath = this.runtimeContext.getParameter("TargetPath");
		URL target = transformUrl(documentBase, targetPath);
		this.runtimeContext.showDocument(target);
	}

	private boolean sendStatement(byte[] statement) throws IOException {
		this.appletView.outputInfoMessage(InfoLevel.NORMAL, this.messages
				.getString("sending"));
		this.appletView.outputDetailMessage("Sending statement...");
		URL documentBase = this.runtimeContext.getDocumentBase();
		this.appletView.outputDetailMessage("document base: " + documentBase);
		String servletPath = this.runtimeContext.getParameter("ServletPath");
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
			this.appletView
					.outputDetailMessage("Statement successfully transmitted.");
			return true;
		}
		String safeOnlineResultCode = httpURLConnection
				.getHeaderField(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER);
		if (SharedConstants.PERMISSION_DENIED_ERROR
				.equals(safeOnlineResultCode)) {
			this.appletView
					.outputDetailMessage("PERMISSION DENIED. YOUR EID MIGHT BE IN USE BY ANOTHER USER");
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("permissionDenied"));
			return false;
		}
		if (SharedConstants.SUBSCRIPTION_NOT_FOUND_ERROR
				.equals(safeOnlineResultCode)) {
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("notSubscribed"));
			return false;
		}
		if (SharedConstants.SUBJECT_NOT_FOUND_ERROR
				.equals(safeOnlineResultCode)) {
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString("eIdNotRegistered"));
			return false;
		}
		throw new IOException("Response code: " + responseCode);
	}

	public char[] getPin() {
		JLabel promptLabel = new JLabel(this.messages.getString("pinQuestion"));

		JPasswordField passwordField = new JPasswordField(8);
		passwordField.setEchoChar('*');

		Box passwordPanel = Box.createHorizontalBox();
		passwordPanel.add(promptLabel);
		passwordPanel.add(Box.createHorizontalStrut(5));
		passwordPanel.add(passwordField);

		int result = JOptionPane.showOptionDialog(null, passwordPanel,
				this.messages.getString("pinTitle"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			char[] pin = passwordField.getPassword();
			return pin;
		}
		return null;
	}

	public static URL transformUrl(URL documentBase, String targetPath) {
		if (targetPath.startsWith("http://")
				|| targetPath.startsWith("https://"))
			try {
				return new URL(targetPath);
			} catch (MalformedURLException e) {
				throw new RuntimeException("URL error: " + e.getMessage());
			}

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
}
