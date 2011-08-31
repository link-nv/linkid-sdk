package net.link.safeonline.config.ws.json;

import static com.google.common.base.Preconditions.*;

import net.link.safeonline.config.ws.ConfigurationStatusCode;
import org.jetbrains.annotations.NotNull;


/**
 * <i>06 10, 2011</i>
 *
 * @author lhunath
 */
public class ConfigurationOperationFailedException extends Exception {

    private final ConfigurationStatusCode status;

    public ConfigurationOperationFailedException(@NotNull final Throwable cause, @NotNull final ConfigurationStatusCode status) {

        super( cause.toString(), cause );

        this.status = checkNotNull( status );
    }

    public ConfigurationOperationFailedException(@NotNull final ConfigurationStatusCode status, @NotNull final String errorMessage) {

        super( errorMessage, null );

        this.status = checkNotNull( status );
    }

    public ConfigurationOperationFailedException(@NotNull final Throwable cause, @NotNull final ConfigurationStatusCode status,
                                                 @NotNull final String errorMessage) {

        super( errorMessage, cause );

        this.status = checkNotNull( status );
    }

    public ConfigurationStatusCode getCode() {

        return status;
    }
}
