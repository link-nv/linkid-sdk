package net.link.safeonline.sdk.api.ws.linkid.ltqr;

/**
 * Created by wvdhaute
 * Date: 21/10/15
 * Time: 16:57
 */
public enum LinkIDLTQRLockType {

    NEVER,              // as in never!
    ON_SCAN,            // as in first user that scans ( e.g. start a linkID QR session from the LTQR ) locks the LTQR until a change op with unlock=true
    ON_FINISH         // as in first user that finishes ( authenticated or payment order payed ) locks the LTQR until a change op with unlock=true
}
