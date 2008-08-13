/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.util.Observable;


/**
 * This class manages the logging buffers sent by LogFilter. Other observers can subscribe to this class to get updates
 * of those messages.
 *
 *
 * @author wvdhaute
 *
 */
public class LogManager extends Observable {

    private static LogManager logManager = null;


    public static LogManager getInstance() {

        if (null == logManager) {
            logManager = new LogManager();
        }
        return logManager;
    }

    public void postLogBuffer(StringBuffer logBuffer) {

        setChanged();
        notifyObservers(logBuffer);
    }
}
