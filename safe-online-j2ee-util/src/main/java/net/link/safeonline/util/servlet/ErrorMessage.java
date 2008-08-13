/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.util.servlet;

/**
 * Wrapper class that hols a name-value pair for error messages. Used by {@link AbstractInjectionServlet}.
 *
 * @author wvdhaute
 *
 */
public class ErrorMessage {

    private String name;

    private String message;


    public ErrorMessage(String message) {

        this.name = "ErrorMessage";
        this.message = message;
    }

    public ErrorMessage(String name, String message) {

        this.name = name;
        this.message = message;
    }

    public String getName() {

        return this.name;
    }

    public String getMessage() {

        return this.message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

}
