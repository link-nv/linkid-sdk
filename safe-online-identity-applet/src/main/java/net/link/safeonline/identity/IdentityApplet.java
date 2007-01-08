/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.identity;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.link.safeonline.p11sc.SmartCard;
import net.link.safeonline.p11sc.SmartCardConfigFactory;
import net.link.safeonline.p11sc.SmartCardFactory;
import net.link.safeonline.p11sc.impl.XmlSmartCardConfigFactory;

public class IdentityApplet extends JApplet {

	private static final long serialVersionUID = 1L;

	public IdentityApplet() {
		JTextArea outputArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(outputArea);
		add(scrollPane);

		SmartCard smartCard = SmartCardFactory.newInstance();

		SmartCardConfigFactory configFactory = new XmlSmartCardConfigFactory();
		smartCard.init(configFactory.getSmartCardConfigs());

		smartCard.open();

		String givenName = smartCard.getGivenName();
		outputArea.append("given name: " + givenName + "\n");

		IdentityStatementFactory identityStatementFactory = new IdentityStatementFactory();
		String identityStatement = identityStatementFactory
				.createIdentityStatement(smartCard);

		outputArea.append("identity statement: " + identityStatement);

		smartCard.close();
	}
}
