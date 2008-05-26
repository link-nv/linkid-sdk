/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import net.link.safeonline.entity.HistoryEntity;

/**
 * Takes a history entity and formats an internationalized message from it.
 * 
 * @author wvdhaute
 * 
 */
public class HistoryMessageManager {

	public static final String RESOURCE_BASE = "messages.history";

	public static String getMessage(FacesContext context,
			HistoryEntity historyEntity) {

		Locale locale = context.getExternalContext().getRequestLocale();
		ResourceBundle messages = ResourceBundle.getBundle(RESOURCE_BASE,
				locale);

		String message = messages.getString(historyEntity.getEvent().getKey());

		if (null == message)
			return messages.getString("history_unknown");

		return MessageFormat.format(message, historyEntity.getSubject()
				.getUserId(), historyEntity.getApplication(), historyEntity
				.getInfo());
	}
}
