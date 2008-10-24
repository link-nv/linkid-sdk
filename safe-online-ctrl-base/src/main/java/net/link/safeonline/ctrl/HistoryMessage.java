/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl;

import java.util.Date;


/**
 * Small wrapper class used by view.
 * 
 * @author wvdhaute
 * 
 */
public class HistoryMessage {

    private Date   when;
    private String message;


    public HistoryMessage(Date when, String message) {

        this.when = when;
        this.message = message;
    }

    public Date getWhen() {

        return this.when;
    }

    public String getMessage() {

        return this.message;
    }
}
