/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.common;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import net.link.safeonline.applet.AppletController;
import net.link.safeonline.applet.AppletView;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.applet.RuntimeContext;
import net.link.safeonline.applet.StatementProvider;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages.KEY;
import net.link.safeonline.shared.SharedConstants;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;


public class PcscAppletController implements AppletController, PcscSignerLogger {

    public static final byte[]     BEID_ATR_11  = new byte[] { 0x3b, (byte) 0x98, 0x13, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01, 0x01,
            (byte) 0xad, 0x13, 0x11            };

    public static final byte[]     BEID_ATR_10  = new byte[] { 0x3b, (byte) 0x98, (byte) 0x94, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01,
            0x01, (byte) 0xad, 0x13, 0x10      };

    public static final byte[]     BEID_ATR_100 = new byte[] { 0x3b, (byte) 98, (byte) 0x94, 0x40, (byte) 0xff, (byte) 0xa5, 0x03, 0x01,
            0x01, 0x01, (byte) 0xad, 0x13, 0x10 };

    private AppletView             appletView;

    private RuntimeContext         runtimeContext;

    private StatementProvider      statementProvider;

    private AuthenticationMessages messages;


    public void init(AppletView newAppletView, RuntimeContext newRuntimeContext, StatementProvider newStatementProvider) {

        appletView = newAppletView;
        runtimeContext = newRuntimeContext;
        statementProvider = newStatementProvider;

        Locale locale = runtimeContext.getLocale();
        messages = new AuthenticationMessages(locale);
    }

    public void abort() {

    }

    public void run() {

        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.START));
        Card card;
        try {
            card = openCard();
        } catch (NoReaderException e) {
            showPath("missing-reader.seam");
            return;
        }
        if (null == card)
            return;
        try {
            CardChannel channel = card.getBasicChannel();
            Signer signer = new PcscSigner(channel, this, messages);
            IdentityProvider identityProvider = new PcscIdentityProvider(channel);
            byte[] statement = statementProvider.createStatement(signer, identityProvider);
            try {
                boolean result = sendStatement(statement);
                if (false == result)
                    return;
            } catch (IOException e) {
                appletView.outputDetailMessage("IO error: " + e.getMessage());
                appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
                return;
            }
        } catch (Exception e) {
            appletView.outputDetailMessage("error: " + e.getMessage());
            appletView.outputDetailMessage("error type: " + e.getClass().getName());
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                appletView.outputDetailMessage(stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "("
                        + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")");
            }
            return;
        } finally {
            closeCard(card);
        }
        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.DONE));
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

        path += "?cacheid=" + Math.random() * 1000000;
        appletView.outputDetailMessage("redirecting to: " + path);

        URL url = transformUrl(documentBase, path);
        runtimeContext.showDocument(url);
    }

    private boolean sendStatement(byte[] statement)
            throws IOException {

        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.SENDING));
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
            appletView.outputDetailMessage("PERMISSION DENIED. YOUR EID MIGHT BE IN USE BY ANOTHER USER");
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.PERMISSION_DENIED));
            return false;
        }
        if (SharedConstants.SUBSCRIPTION_NOT_FOUND_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.NOT_SUBSCRIBED));
            return false;
        }
        if (SharedConstants.SUBJECT_NOT_FOUND_ERROR.equals(safeOnlineResultCode)) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.EID_NOT_REGISTERED));
            appletView.outputDetailMessage(messages.getString(KEY.EID_NOT_REGISTERED));
            appletView.outputDetailMessage("Please login with another authentication device first.");
            return false;
        }
        throw new IOException("Response code: " + responseCode);
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

    private void closeCard(Card card) {

        try {
            card.disconnect(false);
        } catch (CardException e) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
            appletView.outputDetailMessage("error message: " + e.getMessage());
        }
    }

    private Card openCard()
            throws NoReaderException {

        TerminalFactory factory = TerminalFactory.getDefault();
        appletView.outputDetailMessage("terminal factory type: " + factory.getType());
        CardTerminals terminals = factory.terminals();
        List<CardTerminal> terminalList;
        try {
            terminalList = terminals.list();
            if (0 == terminalList.size()) {
                appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.NO_READER));
                appletView.outputDetailMessage("No card reader available or missing card reader driver.");
                throw new NoReaderException();
            }
            for (CardTerminal cardTerminal : terminalList) {
                appletView.outputDetailMessage("trying card terminal: " + cardTerminal.getName());
                if (false == cardTerminal.isCardPresent()) {
                    appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.NO_CARD));
                    if (false == cardTerminal.waitForCardPresent(0)) {
                        appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
                        return null;
                    }
                }
                Card card = cardTerminal.connect("T=0");
                ATR atr = card.getATR();
                byte[] atrBytes = atr.getBytes();
                if (false == Arrays.equals(BEID_ATR_11, atrBytes) && false == Arrays.equals(BEID_ATR_10, atrBytes)
                        && false == Arrays.equals(BEID_ATR_100, atrBytes)) {
                    continue;
                }
                return card;
            }
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.NO_BEID));
            return null;
        } catch (CardException e) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
            appletView.outputDetailMessage("error message: " + e.getMessage());
            return null;
        }
    }

    public void log(String message) {

        appletView.outputDetailMessage(message);
    }
}
