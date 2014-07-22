package net.link.safeonline.sdk.example.rest;

import com.sun.jersey.api.core.PackagesResourceConfig;
import javax.ws.rs.ApplicationPath;


@ApplicationPath("restv1")
public class RestApplication extends PackagesResourceConfig {

    public RestApplication() {

        super( RestApplication.class.getPackage().getName() );
    }
}
