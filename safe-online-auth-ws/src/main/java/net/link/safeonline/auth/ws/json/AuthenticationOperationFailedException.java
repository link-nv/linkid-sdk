package net.link.safeonline.auth.ws.json;

import static com.google.common.base.Preconditions.*;

import net.link.safeonline.auth.ws.AuthenticationStatusCode;
import org.jetbrains.annotations.NotNull;


/**
 * <i>06 10, 2011</i>
 *
 * @author lhunath
 */
public class AuthenticationOperationFailedException extends Exception {

    private final AuthenticationStatusCode status;

    public AuthenticationOperationFailedException(@NotNull final Throwable cause, @NotNull final AuthenticationStatusCode status) {

        super( cause.toString(), cause );

        this.status = checkNotNull( status );
    }

    public AuthenticationOperationFailedException(@NotNull final AuthenticationStatusCode status, @NotNull final String errorMessage) {

        super( errorMessage, null );

        this.status = checkNotNull( status );
    }

    public AuthenticationOperationFailedException(@NotNull final Throwable cause, @NotNull final AuthenticationStatusCode status,
                                                  @NotNull final String errorMessage) {

        super( errorMessage, cause );

        this.status = checkNotNull( status );
    }

    public AuthenticationStatusCode getCode() {

        return status;
    }
}
