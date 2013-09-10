package net.link.safeonline.attribute.provider.docdata;

public interface DocDataConstants {

    // attribute names
    String TOKEN              = "urn:com:docdata:token";
    String TOKEN_ID           = "urn:com:docdata:token:id";
    String TOKEN_PRETTY_PRINT = "urn:com:docdata:token:prettyprint";
    String TOKEN_PRIORITY     = "urn:com:docdata:token:priority";
    String TOKEN_TYPE         = "urn:com:docdata:token:type";

    // PKI
    String TRUST_DOMAIN = "DocData";

    // configuration
    String configGroup     = "DocData Attribute Provider";
    String configMaxTokens = "Maximum # of tokens";
}
