package net.link.safeonline.auth.ws.json;

import net.link.safeonline.auth.ws.AuthenticationErrorCode;
import net.link.safeonline.auth.ws.soap.AuthenticationStep;


/**
 * <h2>{@link AuthenticationResponse}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>12 01, 2010</i> </p>
 *
 * @author lhunath
 */
public class AuthenticationResponse {

    private final AuthenticationStep      nextStep;
    private final AuthenticationErrorCode error;
    private final String                  errorMessage;

    private AuthenticationResponse(final AuthenticationStep nextStep, final AuthenticationErrorCode error, final String errorMessage) {

        this.nextStep = nextStep;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public AuthenticationResponse(final AuthenticationStep nextStep) {

        this( nextStep, null, null );
    }

    public AuthenticationResponse(final AuthenticationErrorCode error, final String errorMessage) {

        this( null, error, errorMessage );
    }

    public AuthenticationStep getNextStep() {

        return nextStep;
    }

    public AuthenticationErrorCode getError() {

        return error;
    }

    public String getErrorMessage() {

        return errorMessage;
    }
}
