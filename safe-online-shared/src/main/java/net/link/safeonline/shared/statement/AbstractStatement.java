/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.shared.statement;

import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.asn1.statement.AbstractDERStatement;

abstract public class AbstractStatement {

    private final AbstractDERStatement derStatement;
    private final Signer               signer;


    public AbstractStatement(Signer signer, AbstractDERStatement derStatement) {

        this.derStatement = derStatement;
        this.signer = signer;
    }

    public byte[] generateStatement() {

        byte[] tbs = this.derStatement.getToBeSignedEncoded();
        byte[] signatureValue = this.signer.sign(tbs);
        this.derStatement.setSignature(signatureValue);
        return this.derStatement.getEncoded();
    }
}
