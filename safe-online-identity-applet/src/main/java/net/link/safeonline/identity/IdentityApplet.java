/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import java.applet.AppletContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.impl.XmlSmartCardConfigFactory;

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

		SmartCardConfigFactory configFactory = new XmlSmartCardConfigFactory();
		smartCard.init(configFactory.getSmartCardConfigs());

		output("Connecting to smart card...");
		smartCard.open();

		String givenName = smartCard.getGivenName();
		String surname = smartCard.getSurname();
		output("given name: " + givenName);
		output("surname: " + surname);

		output("Creating identity statement...");
		IdentityStatementFactory identityStatementFactory = new IdentityStatementFactory();
		String identityStatement = identityStatementFactory
				.createIdentityStatement(smartCard);

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
		appletContext.showDocument(documentBase);
	}

	private void sendIdentityStatement(String identityStatement)
			throws IOException {
		output("Sending identity statement...");
		URL documentBase = getDocumentBase();
		output("document base: " + documentBase);
		URL url = new URL("http://localhost:8080/safe-online/identity/");
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setRequestProperty("Content-type", "text/xml");
		httpURLConnection.setDoOutput(true);
		OutputStream outputStream = httpURLConnection.getOutputStream();
		outputStream.write(identityStatement.getBytes());
		outputStream.close();
		httpURLConnection.connect();

		httpURLConnection.disconnect();

		int responseCode = httpURLConnection.getResponseCode();
		if (200 == responseCode) {
			output("Identity statement successfully transmitted.");
			return;
		}
		throw new IOException("Response code: " + responseCode);
	}

}
