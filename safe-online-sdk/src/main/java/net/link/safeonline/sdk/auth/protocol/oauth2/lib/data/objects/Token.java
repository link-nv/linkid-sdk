package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects;

import java.io.Serializable;
import java.util.Date;


/**
 * TODO description
 * <p/>
 * Date: 03/05/12
 * Time: 15:45
 *
 * @author sgdesmet
 */
public class Token implements Serializable {

    protected String  tokenData;
    protected Date    expirationDate;
    protected boolean invalid;

    public Token(final String tokenData, final Date expirationDate, final boolean invalid) {

        this.tokenData = tokenData;
        this.expirationDate = expirationDate;
        this.invalid = invalid;
    }

    public Token(final String tokenData, final Date expirationDate) {

        this( tokenData, expirationDate, false );
    }

    public boolean isInvalid() {

        return invalid;
    }

    public void setInvalid(final boolean invalid) {

        this.invalid = invalid;
    }

    public String getTokenData() {

        return tokenData;
    }

    public void setTokenData(final String tokenData) {

        this.tokenData = tokenData;
    }

    public Date getExpirationDate() {

        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {

        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Token))
            return false;

        Token token = (Token) o;

        return !(tokenData != null? !tokenData.equals( token.tokenData ): token.tokenData != null);
    }

    @Override
    public int hashCode() {

        return tokenData != null? tokenData.hashCode(): 0;
    }

    @Override
    public String toString() {

        return String.format( "%s{tokenData='%s', expirationDate=%s, invalid=%s}", getClass().getName(), tokenData, expirationDate,
                invalid );
    }
}
