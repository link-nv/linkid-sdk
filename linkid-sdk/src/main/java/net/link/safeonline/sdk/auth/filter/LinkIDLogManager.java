/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.util.Observable;


/**
 * This class manages the logging buffers sent by LogFilter. Other observers can subscribe to this class to get updates of those messages.
 *
 * @author wvdhaute
 */
public class LinkIDLogManager extends Observable {

    private static LinkIDLogManager linkIDLogManager = null;

    public static LinkIDLogManager getInstance() {

        if (null == linkIDLogManager)
            linkIDLogManager = new LinkIDLogManager();
        return linkIDLogManager;
    }

    public void postLogBuffer(StringBuffer logBuffer) {

        setChanged();
        notifyObservers( logBuffer );
    }
}
