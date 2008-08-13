package net.link.safeonline.demo.model.bean;

import java.security.cert.X509Certificate;

import javax.ejb.Stateless;

import net.link.safeonline.demo.model.WSSecurityConfiguration;


@Stateless
public class WSSecurityConfigurationBean implements WSSecurityConfiguration {

    public long getMaximumWsSecurityTimestampOffset() {

        return 1000 * 60 * 5L;
    }

    public boolean skipMessageIntegrityCheck(X509Certificate certificate) {

        return false;
    }

}
