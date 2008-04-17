/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

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
import javax.smartcardio.CardTerminals.State;

import net.link.safeonline.applet.AppletController;
import net.link.safeonline.applet.AppletView;
import net.link.safeonline.applet.InfoLevel;
import net.link.safeonline.applet.RuntimeContext;
import net.link.safeonline.applet.StatementProvider;
import net.link.safeonline.auth.pcsc.AuthenticationMessages.KEY;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;

public class PcscAppletController implements AppletController, PcscSignerLogger {

	public static final byte[] BEID_ATR_11 = new byte[] { 0x3b, (byte) 0x98,
			0x13, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01, 0x01, (byte) 0xad,
			0x13, 0x11 };

	public static final byte[] BEID_ATR_10 = new byte[] { 0x3b, (byte) 0x98,
			(byte) 0x94, 0x40, 0x0a, (byte) 0xa5, 0x03, 0x01, 0x01, 0x01,
			(byte) 0xad, 0x13, 0x10 };

	private AppletView appletView;

	private RuntimeContext runtimeContext;

	private StatementProvider statementProvider;

	private AuthenticationMessages messages;

	public void init(AppletView appletView, RuntimeContext runtimeContext,
			StatementProvider statementProvider) {
		this.appletView = appletView;
		this.runtimeContext = runtimeContext;
		this.statementProvider = statementProvider;

		Locale locale = this.runtimeContext.getLocale();
		this.messages = new AuthenticationMessages(locale);
	}

	public void run() {
		this.appletView.outputInfoMessage(InfoLevel.NORMAL, this.messages
				.getString(KEY.START));
		Card card = openCard();
		if (null == card) {
			return;
		}
		try {
			CardChannel channel = card.getBasicChannel();
			Signer signer = new PcscSigner(channel, this);
			IdentityProvider identityProvider = new PcscIdentityProvider(
					channel);
			this.statementProvider.createStatement(signer, identityProvider);
			// TODO: send the statement
		} finally {
			closeCard(card);
		}
	}

	private void closeCard(Card card) {
		try {
			card.disconnect(false);
		} catch (CardException e) {
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString(KEY.ERROR));
			this.appletView.outputDetailMessage("error message: "
					+ e.getMessage());
		}
	}

	private Card openCard() {
		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals terminals = factory.terminals();
		List<CardTerminal> terminalList;
		try {
			terminalList = terminals.list(State.CARD_PRESENT);
			for (CardTerminal cardTerminal : terminalList) {
				Card card = cardTerminal.connect("T=0");
				ATR atr = card.getATR();
				byte[] atrBytes = atr.getBytes();
				if (false == Arrays.equals(BEID_ATR_11, atrBytes)
						&& false == Arrays.equals(BEID_ATR_10, atrBytes)) {
					continue;
				}
				return card;
			}
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString(KEY.NO_BEID));
			return null;
		} catch (CardException e) {
			this.appletView.outputInfoMessage(InfoLevel.ERROR, this.messages
					.getString(KEY.ERROR));
			this.appletView.outputDetailMessage("error message: "
					+ e.getMessage());
			return null;
		}
	}

	@Override
	public void log(String message) {
		this.appletView.outputDetailMessage(message);
	}
}
