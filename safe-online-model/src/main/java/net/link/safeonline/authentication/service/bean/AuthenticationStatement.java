/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import net.link.safeonline.authentication.exception.DecodingException;


/**
 * Authentication Statement object class. Holds the structure and parser for the client-side generated authentication
 * statement. Can also verify the signature on the statement.
 *
 * @author fcorneli
 *
 */
public class AuthenticationStatement extends AbstractStatement<AuthenticationStatementStructure> {

    public AuthenticationStatement(byte[] encodedAuthenticationStatement) throws DecodingException {

        super(new AuthenticationStatementStructure(encodedAuthenticationStatement));
    }

    public String getSessionId() {

        return super.getStatementStructure().getSessionId();
    }

    public String getApplicationId() {

        return super.getStatementStructure().getApplicationId();
    }
}
