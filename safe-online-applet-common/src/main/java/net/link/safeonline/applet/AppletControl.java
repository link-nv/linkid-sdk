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

import net.link.safeonline.p11sc.MissingSmartCardReaderException;
import net.link.safeonline.p11sc.NoPkcs11LibraryException;
import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfig;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.SmartCardInteraction;
import net.link.safeonline.p11sc.SmartCardNotFoundException;
import net.link.safeonline.p11sc.SmartCardPinCallback;
import net.link.safeonline.p11sc.UnsupportedSmartCardException;
import net.link.safeonline.p11sc.impl.SmartCardConfigFactoryImpl;
import net.link.safeonline.p11sc.impl.SmartCardImpl;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.logging.Log;

import sun.security.pkcs11.wrapper.PKCS11Exception;


/**
 * Applet control component for PKCS#11 smart cards.
 * 
 * @author fcorneli
 * 
 */
public class AppletControl implements AppletController, SmartCardPinCallback, SmartCardInteraction {

    private AppletView        appletView;

    private RuntimeContext    runtimeContext;

    private StatementProvider statementProvider;

    private ResourceBundle    messages;

    private SmartCard         smartCard;


    private void setupLogging() {

        Log log = appletView.getLog();
        SmartCardImpl.setLog(log);
    }

    public void run() {

        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString("connectingToSmartCard"));
        appletView.outputDetailMessage("Loading smart card component...");
        smartCard = SmartCardFactory.newInstance();

        setupLogging();

        SmartCardConfigFactory configFactory = new SmartCardConfigFactoryImpl();
        List<SmartCardConfig> smartCardConfigs = configFactory.getSmartCardConfigs();
        smartCard.init(smartCardConfigs, this);
        for (SmartCardConfig smartCardConfig : smartCardConfigs) {
            appletView.outputDetailMessage("smart card config available for: " + smartCardConfig.getCardAlias());
        }

        String smartCardAlias = runtimeContext.getParameter("SmartCardConfig");

        appletView.outputDetailMessage("Connecting to smart card...");
        String osName = System.getProperty("os.name");
        appletView.outputDetailMessage("os name: " + osName);

        smartCard.setSmartCardPinCallback(this);

        try {
            smartCard.open(smartCardAlias);
        } catch (NoPkcs11LibraryException e) {
            appletView.outputDetailMessage("no PKCS#11 library found");
            showDocument("NoPkcs11Path");
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();
            return;
        } catch (MissingSmartCardReaderException e) {
            appletView.outputDetailMessage("missing smart card reader");
            showPath("missing-reader.seam");
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();
            return;
        } catch (SmartCardNotFoundException e) {
            appletView.outputDetailMessage("smart card not found");
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("smartCardNotFound"));
            /*
             * TODO: retry somehow? is difficult via pkcs11
             */
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();

            return;
        } catch (UnsupportedSmartCardException e) {
            appletView.outputDetailMessage("unsupported smart card");
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("noBeID"));
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();
            return;
        } catch (Exception e) {
            appletView.outputDetailMessage("error opening the smart card: " + e.getMessage());
            appletView.outputDetailMessage("error type: " + e.getClass().getName());
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("smartCardConnectError"));
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                appletView.outputDetailMessage(stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + " ("
                        + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
            }
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();

            return;
        }

        byte[] statement;
        try {
            Pkcs11Signer pkcs11Signer = new Pkcs11Signer(smartCard);
            BeIdIdentityProvider identityProvider = new BeIdIdentityProvider(smartCard);
            statement = statementProvider.createStatement(pkcs11Signer, identityProvider);
        } catch (ProviderException e) {
            Throwable cause = e.getCause();
            if (cause instanceof PKCS11Exception) {
                smartCard.close();
                smartCard.resetPKCS11Driver();
                try {
                    smartCard.open(smartCardAlias);
                    Pkcs11Signer pkcs11Signer = new Pkcs11Signer(smartCard);
                    BeIdIdentityProvider identityProvider = new BeIdIdentityProvider(smartCard);
                    statement = statementProvider.createStatement(pkcs11Signer, identityProvider);
                } catch (Exception e2) {
                    appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("signErrorMsg"));
                    appletView.outputDetailMessage("error signing the statement: " + e2.getMessage());
                    return;
                }
            } else {
                appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("signErrorMsg"));
                appletView.outputDetailMessage("error signing the statement: " + e.getMessage());
                return;
            }
        } catch (Exception e) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("statementError"));
            appletView.outputDetailMessage("error creating the statement: " + e.getMessage());
            return;
        } finally {
            appletView.outputDetailMessage("Disconnecting from smart card...");
            smartCard.close();
            smartCard.resetPKCS11Driver();
        }

        try {
            if (false == sendStatement(statement))
                return;
        } catch (IOException e) {
            appletView.outputDetailMessage("Error occurred while sending the statement");
            appletView.outputDetailMessage("IO error: " + e.getMessage());
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("errorSending"));
            return;
        } catch (Exception e) {
            appletView.outputDetailMessage("Error occurred while sending the statement");
            appletView.outputDetailMessage("Error: " + e.getMessage());
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("errorSending"));
            return;
        }
        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString("done"));
        appletView.outputDetailMessage("Done.");

        showDocument("TargetPath");
    }

    private void showPath(String path) {

        URL documentBase = runtimeContext.getDocumentBase();
        URL url = transformUrl(documentBase, path);
        runtimeContext.showDocument(url);
    }

    private void showDocument(String runtimeParameter) {

        URL documentBase = runtimeContext.getDocumentBase();
        String path = runtimeContext.getParameter(runtimeParameter);
        if (null == path) {
            appletView.outputDetailMessage("runtime parameter not set: " + runtimeParameter);
            return;
        }
        URL url = transformUrl(documentBase, path);
        runtimeContext.showDocument(url);
    }

    private boolean sendStatement(byte[] statement)
            throws IOException {

        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString("sending"));
        appletView.outputDetailMessage("Sending statement...");
        URL documentBase = runtimeContext.getDocumentBase();
        appletView.outputDetailMessage("document base: " + documentBase);
        String servletPath = runtimeContext.getParameter("ServletPath");
        URL url = transformUrl(documentBase, servletPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setAllowUserInteraction(false);
        httpURLConnection.setRequestProperty("Content-type", "application/octet-stream");
        httpURLConnection.setDoOutput(true);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(statement);
        outputStream.close();

        httpURLConnection.connect();

        httpURLConnection.disconnect();

        int responseCode = httpURLConnection.getResponseCode();
        if (200 == responseCode) {
            appletView.outputDetailMessage("Statement successfully transmitted.");
            return true;
        }
        String safeOnlineResultCode = httpURLConnection.getHeaderField(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER);
        if (SharedConstants.PERMISSION_DENIED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputDetailMessage("PERMISSION DENIED. Invalid statement");
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("permissionDenied"));
            return false;
        }
        if (SharedConstants.SUBSCRIPTION_NOT_FOUND_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("notSubscribed"));
            return false;
        }
        if (SharedConstants.SUBJECT_NOT_FOUND_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("eIdNotRegistered"));
            return false;
        }
        if (SharedConstants.DEVICE_DISABLED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("eIdDisabled"));
            return false;
        }
        if (SharedConstants.ALREADY_REGISTERED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("eIdAlreadyRegistered"));
            return false;
        }
        if (SharedConstants.PKI_EXPIRED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("pkiExpired"));
            return false;
        }
        if (SharedConstants.PKI_NOT_YET_VALID_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("pkiNotYetValid"));
            return false;
        }
        if (SharedConstants.PKI_REVOKED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("pkiRevoked"));
            return false;
        }
        if (SharedConstants.PKI_SUSPENDED_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("pkiSuspended"));
            return false;
        }
        if (SharedConstants.PKI_INVALID_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString("pkiInvalid"));
            return false;
        }
        throw new IOException("Response code: " + responseCode);
    }

    public char[] getPin() {

        JLabel promptLabel = new JLabel(messages.getString("pinQuestion"));

        JPasswordField passwordField = new JPasswordField(8);
        passwordField.setEchoChar('*');

        Box passwordPanel = Box.createHorizontalBox();
        passwordPanel.add(promptLabel);
        passwordPanel.add(Box.createHorizontalStrut(5));
        passwordPanel.add(passwordField);

        int result = JOptionPane.showOptionDialog(null, passwordPanel, messages.getString("pinTitle"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.OK_OPTION) {
            char[] pin = passwordField.getPassword();
            return pin;
        }
        return null;
    }

    public static URL transformUrl(URL documentBase, String targetPath) {

        if (targetPath.startsWith("http://") || targetPath.startsWith("https://")) {
            try {
                return new URL(targetPath);
            } catch (MalformedURLException e) {
                throw new RuntimeException("URL error: " + e.getMessage());
            }
        }

        String documentBaseStr = documentBase.toString();
        int idx = documentBaseStr.lastIndexOf("/");
        String identityUrlStr = documentBaseStr.substring(0, idx + 1) + targetPath;
        try {
            return new URL(identityUrlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL error: " + e.getMessage());
        }
    }

    public void init(AppletView newAppletView, RuntimeContext newRuntimeContext, StatementProvider newStatementProvider) {

        appletView = newAppletView;
        runtimeContext = newRuntimeContext;
        statementProvider = newStatementProvider;
        Locale locale = runtimeContext.getLocale();
        messages = ResourceBundle.getBundle("net.link.safeonline.applet.ControlMessages", locale);
    }

    public void abort() {

        smartCard.close();
        smartCard.resetPKCS11Driver();
    }

    public Locale getLocale() {

        Locale locale = runtimeContext.getLocale();
        return locale;
    }

    public void output(String message) {

        appletView.outputInfoMessage(InfoLevel.NORMAL, message);
    }
}
