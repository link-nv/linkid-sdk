/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sc.pcsc.identification;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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

import net.link.safeonline.applet.AppletControl;
import net.link.safeonline.applet.AppletController;
import net.link.safeonline.applet.AppletView;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.applet.RuntimeContext;
import net.link.safeonline.applet.StatementProvider;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages;
import net.link.safeonline.sc.pcsc.auth.AuthenticationMessages.KEY;
import net.link.safeonline.sc.pcsc.common.AddressFile;
import net.link.safeonline.sc.pcsc.common.IdentityFile;
import net.link.safeonline.sc.pcsc.common.Pcsc;


public class IdentificationController implements AppletController {

    public static final byte[]     BEID_ATR_11  = new byte[] { 0x3b, (byte) 0x98, 0x13, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01, 0x01,
            (byte) 0xad, 0x13, 0x11            };

    public static final byte[]     BEID_ATR_10  = new byte[] { 0x3b, (byte) 0x98, (byte) 0x94, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01,
            0x01, (byte) 0xad, 0x13, 0x10      };

    public static final byte[]     BEID_ATR_100 = new byte[] { 0x3b, (byte) 98, (byte) 0x94, 0x40, (byte) 0xff, (byte) 0xa5, 0x03, 0x01,
            0x01, 0x01, (byte) 0xad, 0x13, 0x10 };

    private AppletView             appletView;

    private RuntimeContext         runtimeContext;

    private AuthenticationMessages messages;


    public void init(AppletView newAppletView, RuntimeContext newRuntimeContext, StatementProvider statementProvider) {

        appletView = newAppletView;
        runtimeContext = newRuntimeContext;

        Locale locale = runtimeContext.getLocale();
        messages = new AuthenticationMessages(locale);
    }

    public void abort() {

    }

    public void run() {

        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.START));
        Card card = openCard();
        if (null == card)
            return;
        try {
            CardChannel channel = card.getBasicChannel();
            Pcsc pcsc = new Pcsc(channel);
            try {
                IdentityFile identityFile = pcsc.getIdentityFile();
                AddressFile addressFile = pcsc.getAddressFile();
                appletView.outputDetailMessage("name: " + identityFile.getName());
                appletView.outputDetailMessage("first name: " + identityFile.getFirstName());
                appletView.outputDetailMessage("birth date: " + identityFile.getBirthDate());
                appletView.outputDetailMessage("nationality: " + identityFile.getNationality());
                appletView.outputDetailMessage("sex: " + identityFile.getSex());
                appletView.outputDetailMessage("street + number: " + addressFile.getStreetAndNumber());
                appletView.outputDetailMessage("zip: " + addressFile.getZip());
                appletView.outputDetailMessage("municipality: " + addressFile.getMunicipality());
                postData(identityFile, addressFile);
            } catch (Exception e) {
                appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
                appletView.outputDetailMessage("error message: " + e.getMessage());
            }
        } finally {
            closeCard(card);
        }
        appletView.outputInfoMessage(InfoLevel.NORMAL, messages.getString(KEY.DONE));
        showDocument("TargetPath");
    }

    private void closeCard(Card card) {

        try {
            card.disconnect(false);
        } catch (CardException e) {
            appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.ERROR));
            appletView.outputDetailMessage("error message: " + e.getMessage());
        }
    }

    private Card openCard() {

        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();
        List<CardTerminal> terminalList;
        try {
            terminalList = terminals.list();
            if (0 == terminalList.size()) {
                appletView.outputInfoMessage(InfoLevel.ERROR, messages.getString(KEY.NO_READER));
                return null;
            }
            for (CardTerminal cardTerminal : terminalList) {
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

    private void postData(IdentityFile identityFile, AddressFile addressFile)
            throws IOException {

        HttpURLConnection connection = openDataConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String content = "name=" + URLEncoder.encode(identityFile.getName(), "UTF-8") + "&firstname="
                + URLEncoder.encode(identityFile.getFirstName(), "UTF-8") + "&dob="
                + URLEncoder.encode(formatter.format(identityFile.getBirthDate()), "UTF-8") + "&nationality="
                + URLEncoder.encode(identityFile.getNationality(), "UTF-8") + "&sex="
                + URLEncoder.encode(identityFile.getSex().name(), "UTF-8") + "&street="
                + URLEncoder.encode(addressFile.getStreetAndNumber(), "UTF-8") + "&city="
                + URLEncoder.encode(addressFile.getMunicipality(), "UTF-8") + "&zip=" + URLEncoder.encode(addressFile.getZip(), "UTF-8")
                + "&nnr=" + URLEncoder.encode(identityFile.getNationalNumber(), "UTF-8");
        connection.setRequestProperty("Content-length", Integer.toString(content.getBytes().length));
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(content);
        output.flush();
        output.close();
        connection.getResponseCode();
    }

    private HttpURLConnection openDataConnection()
            throws IOException {

        URL documentBase = runtimeContext.getDocumentBase();
        String servletPath = runtimeContext.getParameter("ServletPath");
        URL url = AppletControl.transformUrl(documentBase, servletPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        return httpURLConnection;
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
}
