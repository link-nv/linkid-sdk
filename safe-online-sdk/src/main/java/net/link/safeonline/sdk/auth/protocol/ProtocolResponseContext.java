package net.link.safeonline.sdk.auth.protocol;

import static com.google.common.base.Preconditions.checkNotNull;

import net.link.util.common.CertificateChain;


/**
 * <h2>{@link ProtocolResponseContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>08 19, 2010</i> </p>
 *
 * @author lhunath
 */
public class ProtocolResponseContext extends ProtocolContext {

    private final ProtocolRequestContext request;
    private final CertificateChain certificateChain;

    /**
     * @param request          The request that caused this response.
     * @param id               A unique ID that will match the response to this request.
     * @param certificateChain Optional certificate chain if protocol response was signed and contained the chain embedded in the
     *                         signature.
     */
    public ProtocolResponseContext(ProtocolRequestContext request, String id, CertificateChain certificateChain) {

        super( id );

        this.request = request;
        this.certificateChain = certificateChain;
    }

    public ProtocolRequestContext getRequest() {

        return checkNotNull( request, "Request not set for %s", this );
    }

    /**
     * @return The certificate chain if the protocol authentication response was signed and the chain was embedded in the signature. Returns
     *         <code>null</code> if this was not the case.
     */
    public CertificateChain getCertificateChain() {
        return certificateChain;
    }
}
