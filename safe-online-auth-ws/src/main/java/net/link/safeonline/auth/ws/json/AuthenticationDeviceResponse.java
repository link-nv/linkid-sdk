package net.link.safeonline.auth.ws.json;

import java.util.Map;


/**
 * <h2>{@link AuthenticationDeviceResponse}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>12 01, 2010</i> </p>
 *
 * @author lhunath
 */
public class AuthenticationDeviceResponse extends AuthenticationResponse {

    private final Map<String, String> deviceProperties;

    public AuthenticationDeviceResponse(final Map<String, String> deviceProperties) {

        super( null );

        this.deviceProperties = deviceProperties;
    }

    public Map<String, String> getDeviceProperties() {

        return deviceProperties;
    }
}
