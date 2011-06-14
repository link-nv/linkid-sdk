package net.link.safeonline.auth.ws.json;

import net.link.safeonline.auth.ws.AuthenticationErrorCode;
import org.jetbrains.annotations.NotNull;


/**
 * <i>06 10, 2011</i>
 *
 * @author lhunath
 */
public class AuthenticationOperationFailedException extends Exception {

    private final AuthenticationErrorCode error;

    public AuthenticationOperationFailedException(@NotNull final Throwable cause, @NotNull final AuthenticationErrorCode error) {

        super( cause.toString(), cause );

        this.error = error;
    }

    public AuthenticationOperationFailedException(@NotNull final AuthenticationErrorCode error, @NotNull final String errorMessage) {

        super( errorMessage, null );

        this.error = error;
    }

    public AuthenticationErrorCode getCode() {

        return error;
    }
}
