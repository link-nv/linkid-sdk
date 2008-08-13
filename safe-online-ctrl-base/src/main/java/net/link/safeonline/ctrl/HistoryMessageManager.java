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

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.entity.HistoryEntity;


/**
 * Takes a history entity and formats an internationalized message from it.
 *
 * @author wvdhaute
 *
 */
public class HistoryMessageManager {

    public static final String RESOURCE_BASE = "messages.history";


    /**
     * Returns the i18n history message. The history properties are used to format this message. Following order is to
     * be used in the resource bundle :
     * <ol>
     * <li>Application</li>
     * <li>Device</li>
     * <li>Attribute</li>
     * <li>Info</li>
     * </ol>
     *
     * @param context
     * @param historyEntity
     * @return
     */
    public static String getMessage(FacesContext context, HistoryEntity historyEntity) {

        Locale locale = context.getExternalContext().getRequestLocale();
        ResourceBundle messages = ResourceBundle.getBundle(RESOURCE_BASE, locale);

        String message = messages.getString(historyEntity.getEvent().getKey());

        String application = null;
        String attribute = null;
        String device = null;
        String info = null;

        if (historyEntity.getProperties().get(SafeOnlineConstants.APPLICATION_PROPERTY) != null) {
            application = historyEntity.getProperties().get(SafeOnlineConstants.APPLICATION_PROPERTY).getValue();
        }
        if (historyEntity.getProperties().get(SafeOnlineConstants.ATTRIBUTE_PROPERTY) != null) {
            attribute = historyEntity.getProperties().get(SafeOnlineConstants.ATTRIBUTE_PROPERTY).getValue();
        }
        if (historyEntity.getProperties().get(SafeOnlineConstants.DEVICE_PROPERTY) != null) {
            device = historyEntity.getProperties().get(SafeOnlineConstants.DEVICE_PROPERTY).getValue();
        }
        if (historyEntity.getProperties().get(SafeOnlineConstants.INFO_PROPERTY) != null) {
            info = historyEntity.getProperties().get(SafeOnlineConstants.INFO_PROPERTY).getValue();
        }

        return MessageFormat.format(message, historyEntity.getSubject().getUserId(), application, device, attribute,
                info);
    }
}
