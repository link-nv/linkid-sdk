/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.applet;

import net.link.safeonline.applet.AppletBase;
import net.link.safeonline.applet.AppletController;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.statement.IdentityProvider;

public class OptionApplet extends AppletBase {

	private static final long serialVersionUID = 1L;

	public OptionApplet() {

		super(new OptionController());
	}

	public OptionApplet(AppletController controller) {
		super(controller);
	}

	/**
	 * Unused
	 */
	@Override
	public byte[] createStatement(Signer signer,
			IdentityProvider identityProvider) {

		return null;
	}

}
