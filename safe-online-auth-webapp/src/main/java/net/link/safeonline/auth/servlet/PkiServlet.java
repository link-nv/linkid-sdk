/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMWriter;


/**
 * Servlet that exports the public key and certificate of the OLAS service that is being used to sign the generated SAML
 * tokens. This service can be useful to service providers that want to verify the correctness of the authentication
 * response tokens themselves.
 * 
 * @author fcorneli
 * 
 */
public class PkiServlet extends AbstractInjectionServlet {

    private static final long     serialVersionUID = 1L;

    private static final Log      LOG              = LogFactory.getLog(PkiServlet.class);

    private IdentityServiceClient client;


    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        LOG.debug("init");
        this.client = new IdentityServiceClient();
    }

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        LOG.debug("doGet");
        X509Certificate certificate = this.client.getCertificate();
        String pemCertificate = toPem(certificate);

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(pemCertificate);
        out.close();
    }

    private static String toPem(Object object) {

        StringWriter buffer = new StringWriter();
        try {
            PEMWriter writer = new PEMWriter(buffer);
            LOG.debug("toPem: " + object.getClass().getName());
            writer.writeObject(object);
            writer.close();
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert object to PEM format: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(buffer);
        }
    }
}
